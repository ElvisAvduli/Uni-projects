<?php
session_start();
require_once('db_connection.php');

// Check if user is logged in
if (!isset($_SESSION['user_id'])) {
    header("Location: login.php");
    exit();
}

// Check if flight search parameters exist in session
if (!isset($_SESSION['flight_search'])) {
    header("Location: index.php");
    exit();
}

$flight_search = $_SESSION['flight_search'];
$departure_airport = $flight_search['departure_airport'];
$arrival_airport = $flight_search['arrival_airport'];
$flight_date = $flight_search['flight_date'];
$passengers_count = $flight_search['passengers_count'];

// Get airports information
$stmt = $conn->prepare("SELECT * FROM airports WHERE code = ?");
$stmt->execute([$departure_airport]);
$departure_airport_info = $stmt->fetch(PDO::FETCH_ASSOC);

$stmt->execute([$arrival_airport]);
$arrival_airport_info = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$departure_airport_info || !$arrival_airport_info) {
    header("Location: index.php");
    exit();
}

// Calculate distance using Haversine formula
function calculateDistance($lat1, $lon1, $lat2, $lon2) {
    $R = 6371; // Earth radius in kilometers
    $dLat = deg2rad($lat2 - $lat1);
    $dLon = deg2rad($lon2 - $lon1);
    $a = sin($dLat/2) * sin($dLat/2) + cos(deg2rad($lat1)) * cos(deg2rad($lat2)) * sin($dLon/2) * sin($dLon/2);
    $c = 2 * atan2(sqrt($a), sqrt(1-$a));
    $distance = $R * $c;
    return $distance;
}

$distance = calculateDistance(
    $departure_airport_info['latitude'], 
    $departure_airport_info['longitude'], 
    $arrival_airport_info['latitude'], 
    $arrival_airport_info['longitude']
);

// Calculate base flight cost
$flight_cost = $distance / 10;

// Get seats that are already booked for this flight
$booked_seats = [];
$stmt = $conn->prepare("
    SELECT p.seat_number as seat
    FROM reservations r
    JOIN passengers p ON r.id = p.reservation_id
    WHERE r.departure_airport_id = ? 
    AND r.arrival_airport_id = ? 
    AND r.flight_date = ?
");
$stmt->execute([$departure_airport_info['id'], $arrival_airport_info['id'], $flight_date]);
$results = $stmt->fetchAll(PDO::FETCH_ASSOC);

foreach ($results as $result) {
    $booked_seats[] = $result['seat'];
}

// Handle seat selection and booking
$error = '';
$success = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['confirm_booking'])) {
    $selected_seats = isset($_POST['selected_seats']) ? $_POST['selected_seats'] : [];
    $passenger_names = isset($_POST['passenger_name']) ? $_POST['passenger_name'] : [];
    $passenger_surnames = isset($_POST['passenger_surname']) ? $_POST['passenger_surname'] : [];
    
    // Validation
    if (count($selected_seats) !== (int)$passengers_count) {
        $error = "Please select exactly " . $passengers_count . " seats. You selected " . count($selected_seats) . ".";
    } else {
        // Validate passenger names
        $valid_names = true;
        for ($i = 1; $i < $passengers_count; $i++) { // Start from 1 to skip the first passenger
            if (!isset($passenger_names[$i]) || !isset($passenger_surnames[$i]) ||
                strlen($passenger_names[$i]) < 3 || strlen($passenger_names[$i]) > 20 || 
                strlen($passenger_surnames[$i]) < 3 || strlen($passenger_surnames[$i]) > 20 ||
                !preg_match('/^[a-zA-Z]+$/', $passenger_names[$i]) || 
                !preg_match('/^[a-zA-Z]+$/', $passenger_surnames[$i])) {
                $valid_names = false;
                break;
            }
        }
        
        if (!$valid_names) {
            $error = "Passenger names must be between 3-20 characters and contain only letters.";
        } else {
            try {
                // Calculate total cost
                $total_cost = 0;
                $seat_costs = [];
                
                foreach ($selected_seats as $seat) {
                    $row = intval(substr($seat, 0, -1));
                    $seat_cost = 0;
                    
                    if ($row === 1 || $row === 11 || $row === 12) {
                        $seat_cost = 20;
                    } elseif ($row >= 2 && $row <= 10) {
                        $seat_cost = 10;
                    }
                    
                    $seat_costs[$seat] = $seat_cost;
                    $total_cost += $seat_cost;
                }
                
                // Calculate taxes and total cost
                $taxes = $departure_airport_info['tax'] + $arrival_airport_info['tax'];
                $flight_total = $taxes + $flight_cost;
                $total_per_passenger = $flight_total + ($total_cost / count($selected_seats));
                $grand_total = $total_per_passenger * $passengers_count;
                
                // Start transaction
                $conn->beginTransaction();
                
                // Create reservation record
                $stmt = $conn->prepare("
                    INSERT INTO reservations 
                    (user_id, departure_airport_id, arrival_airport_id, 
                     flight_date, total_cost, created_at)
                    VALUES (?, ?, ?, ?, ?, NOW())
                ");
                $stmt->execute([
                    $_SESSION['user_id'],
                    $departure_airport_info['id'],  // Use the airport ID from the info
                    $arrival_airport_info['id'],    // Use the airport ID from the info
                    $flight_date,
                    $grand_total
                ]);
                
                $reservation_id = $conn->lastInsertId();
                
                // Create passenger records
                for ($i = 0; $i < $passengers_count; $i++) {
                    $name = ($i === 0) ? $_SESSION['first_name'] : $passenger_names[$i];
                    $surname = ($i === 0) ? $_SESSION['last_name'] : $passenger_surnames[$i];
                    $seat = $selected_seats[$i];
                    
                    $stmt = $conn->prepare("
                        INSERT INTO passengers 
                        (reservation_id, first_name, last_name, seat_number)
                        VALUES (?, ?, ?, ?)
                    ");
                    $stmt->execute([
                        $reservation_id,
                        $name,
                        $surname,
                        $seat
                    ]);
                    
                    // Mark seat as booked
                    $stmt = $conn->prepare("
                        INSERT INTO seats 
                        (flight_date, seat_number, is_booked, reservation_id)
                        VALUES (?, ?, 1, ?)
                        ON DUPLICATE KEY UPDATE is_booked = 1, reservation_id = ?
                    ");
                    $stmt->execute([
                        $flight_date,
                        $seat,
                        $reservation_id,
                        $reservation_id
                    ]);
                }
                
                // Commit transaction
                $conn->commit();
                
                // Set success flag in session and redirect
                $_SESSION['booking_success'] = true;
                $_SESSION['reservation_id'] = $reservation_id; // Save the reservation ID for reference
                
                // Redirect to index page
                header("Location: my_trips.php");
                exit(); // Make sure to exit after redirect
                
            } catch (Exception $e) {
                // Rollback transaction on error
                $conn->rollBack();
                $error = "An error occurred while processing your booking. Please try again. Error: " . $e->getMessage();
                
                // Log the error
                error_log("Booking error: " . $e->getMessage());
            }
        }
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Air DS - Book Flight</title>
    <link rel="stylesheet" href="styles.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <style>
        /* Additional seat selection styles */
        .seat {
            transition: all 0.2s ease;
            cursor: pointer;
            width: 40px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 8px;
            font-size: 0.8rem;
            font-weight: 500;
            margin: 2px;
        }

        .seat.selected {
            background-color: #0066cc !important;
            color: white;
            transform: scale(1.05);
            box-shadow: 0 0 10px rgba(0, 102, 204, 0.5);
        }

        .seat.occupied {
            cursor: not-allowed;
            opacity: 0.7;
            background-color: #ffcdd2 !important;
        }

        .seat:hover:not(.occupied):not(.selected) {
            background-color: #b3d9ff !important;
            transform: scale(1.05);
        }

        .selected-seats-box {
            background-color: #f8f9fa;
            padding: 1rem;
            border-radius: 8px;
            margin: 1rem 0;
            border: 1px solid #dee2e6;
        }

        .selected-seats-list {
            padding: 0.5rem;
            background-color: white;
            border-radius: 4px;
            min-height: 20px;
            border: 1px solid #ced4da;
            font-weight: bold;
            color: #0066cc;
        }

        .seat-map {
            display: flex;
            flex-direction: column;
            gap: 5px;
            margin: 1.5rem 0;
        }

        .seat-row {
            display: flex;
            align-items: center;
            gap: 5px;
        }

        .seat-row-number {
            width: 30px;
            text-align: center;
            font-weight: bold;
            color: #6c757d;
        }

        .aisle-space {
            width: 20px;
        }
    </style>
</head>
<body>
    <?php include('header.php'); ?>
    
    <main>
        <?php if ($error): ?>
            <div class="error-message" style="background-color: #ffeeee; color: #cc0000; padding: 1rem; margin-bottom: 1rem; border-radius: 4px;">
                <?= $error ?>
            </div>
        <?php endif; ?>
        
        <div class="flight-summary">
            <h2>Flight Details</h2>
            <div class="flight-details">
                <p><strong>From:</strong> <?= $departure_airport_info['name'] ?> (<?= $departure_airport ?>)</p>
                <p><strong>To:</strong> <?= $arrival_airport_info['name'] ?> (<?= $arrival_airport ?>)</p>
                <p><strong>Date:</strong> <?= date('F j, Y', strtotime($flight_date)) ?></p>
                <p><strong>Passengers:</strong> <?= $passengers_count ?></p>
                <p><strong>Distance:</strong> <?= number_format($distance, 2) ?> km</p>
            </div>
        </div>
        
        <form method="post" action="" id="bookingForm">
            <div class="passenger-container">
                <h3>Passenger Information</h3>
                
                <?php for ($i = 0; $i < $passengers_count; $i++): ?>
                    <div class="passenger-info">
                        <h4>Passenger <?= $i + 1 ?></h4>
                        <?php if ($i === 0): ?>
                            <!-- First passenger is the logged-in user -->
                            <div class="form-group">
                                <label>First Name</label>
                                <input type="text" value="<?= $_SESSION['first_name'] ?>" disabled>
                            </div>
                            <div class="form-group">
                                <label>Last Name</label>
                                <input type="text" value="<?= $_SESSION['last_name'] ?>" disabled>
                            </div>
                            <input type="hidden" name="passenger_name[0]" value="<?= $_SESSION['first_name'] ?>">
                            <input type="hidden" name="passenger_surname[0]" value="<?= $_SESSION['last_name'] ?>">
                        <?php else: ?>
                            <div class="form-group">
                                <label for="passenger_name_<?= $i ?>">First Name</label>
                                <input type="text" id="passenger_name_<?= $i ?>" name="passenger_name[<?= $i ?>]" required
                                       pattern="[A-Za-z]{3,20}" title="3-20 letters only">
                            </div>
                            <div class="form-group">
                                <label for="passenger_surname_<?= $i ?>">Last Name</label>
                                <input type="text" id="passenger_surname_<?= $i ?>" name="passenger_surname[<?= $i ?>]" required
                                       pattern="[A-Za-z]{3,20}" title="3-20 letters only">
                            </div>
                        <?php endif; ?>
                    </div>
                <?php endfor; ?>
            </div>
            
            <div class="airplane-map">
                <h3>Seat Selection</h3>
                <p>Please select <?= $passengers_count ?> seat(s) for your flight.</p>
                
                <div class="seat-price-info">
                    <div class="seat-price-type">
                        <div class="seat-color-box" style="background-color: #e0f0ff; border: 1px solid #003366;"></div>
                        <span>Available - Free</span>
                    </div>
                    <div class="seat-price-type">
                        <div class="seat-color-box" style="background-color: #fffae0; border: 2px solid gold;"></div>
                        <span>Premium - €10</span>
                    </div>
                    <div class="seat-price-type">
                        <div class="seat-color-box" style="background-color: #fff4e0; border: 2px solid #ff9900;"></div>
                        <span>Extra Premium - €20</span>
                    </div>
                    <div class="seat-price-type">
                        <div class="seat-color-box" style="background-color: #ff6666;"></div>
                        <span>Occupied</span>
                    </div>
                    <div class="seat-price-type">
                        <div class="seat-color-box" style="background-color: #0066cc;"></div>
                        <span>Selected</span>
                    </div>
                </div>
                
                <div class="seat-map">
                    <?php 
                    // Generate seat map - 31 rows with 6 seats each (A-F)
                    for ($row = 1; $row <= 31; $row++): 
                    ?>
                        <div class="seat-row">
                            <div class="seat-row-number"><?= $row ?></div>
                            
                            <?php 
                            $seats = ['A', 'B', 'C', 'D', 'E', 'F'];
                            foreach ($seats as $index => $letter): 
                                $seat_id = $row . $letter;
                                $is_occupied = in_array($seat_id, $booked_seats);
                                $seat_class = $is_occupied ? 'occupied' : 'available';
                                
                                // Add premium class based on row
                                if ($row === 1 || $row === 11 || $row === 12) {
                                    $seat_class .= ' extra-premium';
                                    $seat_bg = $is_occupied ? '#ffcdd2' : '#fff4e0';
                                    $seat_border = '2px solid #ff9900';
                                } elseif ($row >= 2 && $row <= 10) {
                                    $seat_class .= ' premium';
                                    $seat_bg = $is_occupied ? '#ffcdd2' : '#fffae0';
                                    $seat_border = '2px solid gold';
                                } else {
                                    $seat_bg = $is_occupied ? '#ffcdd2' : '#e0f0ff';
                                    $seat_border = '1px solid #003366';
                                }
                                
                                // Add aisle space between C and D
                                if ($letter === 'D') {
                                    echo '<div class="aisle-space"></div>';
                                }
                            ?>
                                <div class="seat <?= $seat_class ?>" data-seat="<?= $seat_id ?>" 
                                    <?= $is_occupied ? 'data-occupied="true"' : '' ?>
                                    style="background-color: <?= $seat_bg ?>; border: <?= $seat_border ?>;">
                                    <?= $seat_id ?>
                                </div>
                            <?php endforeach; ?>
                        </div>
                    <?php endfor; ?>
                </div>
                
                <div id="selected-seats-container" class="selected-seats-box">
                    <h4>Selected Seats</h4>
                    <div id="selected-seats-display" class="selected-seats-list">None</div>
                    <div id="selected-seats-inputs"></div>
                </div>
            </div>
            
            <div class="booking-summary" id="booking-summary" style="display: none;">
                <h3>Booking Summary</h3>
                <div class="summary-content">
                    <div class="summary-section">
                        <h4>Flight</h4>
                        <p><?= $departure_airport_info['name'] ?> (<?= $departure_airport ?>) → 
                           <?= $arrival_airport_info['name'] ?> (<?= $arrival_airport ?>)</p>
                        <p>Date: <?= date('F j, Y', strtotime($flight_date)) ?></p>
                    </div>
                    
                    <div class="summary-section">
                        <h4>Passengers</h4>
                        <div id="passengers-summary"></div>
                    </div>
                    
                    <div class="summary-section">
                        <h4>Costs</h4>
                        <p>Departure Airport Tax: €<?= $departure_airport_info['tax'] ?></p>
                        <p>Arrival Airport Tax: €<?= $arrival_airport_info['tax'] ?></p>
                        <p>Flight Cost (<?= number_format($distance, 2) ?> km ÷ 10): €<?= number_format($flight_cost, 2) ?></p>
                        <p>Seat Cost: €<span id="seat-cost">0.00</span></p>
                        <p><strong>Total Cost per Passenger: €<span id="cost-per-passenger">0.00</span></strong></p>
                        <p><strong>Grand Total (<?= $passengers_count ?> passengers): €<span id="grand-total">0.00</span></strong></p>
                    </div>
                </div>
            </div>
            
            <button type="submit" name="confirm_booking" class="button" id="confirm-button" disabled>Confirm Booking</button>
        </form>
    </main>
    
    <?php include('footer.php'); ?>
    
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const seats = document.querySelectorAll('.seat:not(.occupied)');
            const selectedSeatsDisplay = document.getElementById('selected-seats-display');
            const selectedSeatsInputs = document.getElementById('selected-seats-inputs');
            const bookingSummary = document.getElementById('booking-summary');
            const seatCostDisplay = document.getElementById('seat-cost');
            const costPerPassengerDisplay = document.getElementById('cost-per-passenger');
            const grandTotalDisplay = document.getElementById('grand-total');
            const passengersSummary = document.getElementById('passengers-summary');
            const confirmButton = document.getElementById('confirm-button');
            const requiredPassengers = <?= $passengers_count ?>;
            
            // Debug info
            console.log("Required passengers:", requiredPassengers);
            
            let selectedSeats = [];
            
            // Calculate base costs
            const taxes = <?= $departure_airport_info['tax'] + $arrival_airport_info['tax'] ?>;
            const flightCost = <?= $flight_cost ?>;
            const baseTotal = taxes + flightCost;
            
            seats.forEach(seat => {
                seat.addEventListener('click', function() {
                    // Skip if seat is occupied
                    if (this.classList.contains('occupied')) return;
                    
                    // Use data-seat attribute directly
                    const seatId = this.dataset.seat;
                    const seatIndex = selectedSeats.indexOf(seatId);
                    
                    if (seatIndex > -1) {
                        // Deselect seat if already selected
                        this.classList.remove('selected');
                        selectedSeats.splice(seatIndex, 1);
                        console.log("Removed seat. Current seats:", selectedSeats);
                    } else if (selectedSeats.length < requiredPassengers) {
                        // Select seat if we haven't reached the passenger limit
                        this.classList.add('selected');
                        selectedSeats.push(seatId);
                        console.log("Added seat. Current seats:", selectedSeats);
                    }
                    
                    updateSelectedSeats();
                    updateSummary();
                });
            });
            
            function updateSelectedSeats() {
                // Clear previous inputs
                selectedSeatsInputs.innerHTML = '';
                
                // Update display
                if (selectedSeats.length === 0) {
                    selectedSeatsDisplay.textContent = 'None';
                    selectedSeatsDisplay.style.color = '#666';
                    selectedSeatsDisplay.style.fontWeight = 'normal';
                } else {
                    selectedSeatsDisplay.textContent = selectedSeats.join(', ');
                    selectedSeatsDisplay.style.color = '#0066cc';
                    selectedSeatsDisplay.style.fontWeight = 'bold';
                    
                    // Create hidden inputs for each selected seat
                    selectedSeats.forEach(seat => {
                        const input = document.createElement('input');
                        input.type = 'hidden';
                        input.name = 'selected_seats[]';
                        input.value = seat;
                        selectedSeatsInputs.appendChild(input);
                        console.log("Added hidden input for seat:", seat);
                    });
                }
                
                // Update button state
                console.log(`Selected ${selectedSeats.length} out of ${requiredPassengers} required seats`);
                confirmButton.disabled = selectedSeats.length !== requiredPassengers;
                
                // Show/hide booking summary
                bookingSummary.style.display = selectedSeats.length === requiredPassengers ? 'block' : 'none';
            }
            
            function updateSummary() {
                if (selectedSeats.length === 0) return;
                
                // Calculate seat costs
                let totalSeatCost = 0;
                selectedSeats.forEach(seat => {
                    const row = parseInt(seat.match(/\d+/)[0]);
                    
                    if (row === 1 || row === 11 || row === 12) {
                        totalSeatCost += 20;
                    } else if (row >= 2 && row <= 10) {
                        totalSeatCost += 10;
                    }
                });
                
                const avgSeatCost = totalSeatCost / selectedSeats.length;
                const totalPerPassenger = baseTotal + avgSeatCost;
                const grandTotal = totalPerPassenger * requiredPassengers;
                
                seatCostDisplay.textContent = avgSeatCost.toFixed(2);
                costPerPassengerDisplay.textContent = totalPerPassenger.toFixed(2);
                grandTotalDisplay.textContent = grandTotal.toFixed(2);
                
                // Build passengers summary
                let passengersText = '';
                
                // First passenger is the logged-in user
                passengersText += `<div class="passenger-summary">
                    <strong>1.</strong> <?= htmlspecialchars($_SESSION['first_name']) ?> <?= htmlspecialchars($_SESSION['last_name']) ?>
                    <span class="seat-badge" id="passenger1-seat"></span>
                </div>`;
                
                // Add additional passengers if any
                for (let i = 1; i < requiredPassengers; i++) {
                    const nameInput = document.getElementById(`passenger_name_${i}`);
                    const surnameInput = document.getElementById(`passenger_surname_${i}`);
                    
                    let passengerName = '(Information pending)';
                    if (nameInput && nameInput.value && surnameInput && surnameInput.value) {
                        passengerName = `${nameInput.value} ${surnameInput.value}`;
                    }
                    
                    passengersText += `<div class="passenger-summary">
                        <strong>${i + 1}.</strong> ${passengerName}
                        <span class="seat-badge" id="passenger${i+1}-seat"></span>
                    </div>`;
                }
                
                passengersSummary.innerHTML = passengersText;
                
                // Update seat badges if seats are selected
                if (selectedSeats.length > 0) {
                    for (let i = 0; i < Math.min(selectedSeats.length, requiredPassengers); i++) {
                        const seatBadge = document.getElementById(`passenger${i+1}-seat`);
                        if (seatBadge) {
                            seatBadge.textContent = `Seat: ${selectedSeats[i]}`;
                            seatBadge.style.display = 'inline-block';
                            seatBadge.style.marginLeft = '10px';
                            seatBadge.style.padding = '2px 6px';
                            seatBadge.style.backgroundColor = '#0066cc';
                            seatBadge.style.color = 'white';
                            seatBadge.style.borderRadius = '4px';
                            seatBadge.style.fontSize = '0.8rem';
                        }
                    }
                }
            }
            
            // Form validation
            const form = document.getElementById('bookingForm');
            form.addEventListener('submit', function(e) {
                // Don't prevent default here to allow form submission if validation passes
                
                console.log("Form submit attempted. Selected seats:", selectedSeats);
                console.log("Required passengers:", requiredPassengers);
                
                if (selectedSeats.length !== requiredPassengers) {
                    e.preventDefault(); // Prevent form submission if validation fails
                    alert(`Please select exactly ${requiredPassengers} seat(s). You have selected ${selectedSeats.length}.`);
                    return false;
                }
                
                // Validate all passenger names
                let validNames = true;
                for (let i = 1; i < requiredPassengers; i++) {
                    const nameInput = document.getElementById(`passenger_name_${i}`);
                    const surnameInput = document.getElementById(`passenger_surname_${i}`);
                    if (nameInput && surnameInput) {
                        const name = nameInput.value.trim();
                        const surname = surnameInput.value.trim();
                        
                        if (!name || !surname || name.length < 3 || name.length > 20 || 
                            surname.length < 3 || surname.length > 20 || 
                            !/^[a-zA-Z]+$/.test(name) || !/^[a-zA-Z]+$/.test(surname)) {
                            validNames = false;
                            break;
                        }
                    }
                    if (nameInput && surnameInput) {
                        if (!nameInput.validity.valid || !surnameInput.validity.valid) {
                            validNames = false;
                            break;
                        }
                    }
                }
                
                if (!validNames) {
                    e.preventDefault(); // Prevent form submission if validation fails
                    alert('Please provide valid names for all passengers (3-20 letters only).');
                    return false;
                }
                
                // Form will submit naturally if all validation passes
                console.log("Validation passed, form will submit");

            });
        });
    </script>
</body>
</html>