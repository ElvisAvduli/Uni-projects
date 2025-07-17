<?php
session_start();
require_once('db_connection.php');

// Check if user is logged in
$isLoggedIn = isset($_SESSION['user_id']);

// Get all airports from database
$stmt = $conn->prepare("SELECT * FROM airports ORDER BY name");
$stmt->execute();
$airports = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Process form submission
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $departure_airport = $_POST['departure_airport'];
    $arrival_airport = $_POST['arrival_airport'];
    $flight_date = $_POST['flight_date'];
    $passengers_count = $_POST['passengers'];
    
    // Create a flight search object and store in session
    $_SESSION['flight_search'] = [
        'departure_airport' => $departure_airport,
        'arrival_airport' => $arrival_airport,
        'flight_date' => $flight_date,
        'passengers_count' => $passengers_count
    ];
    
    // Redirect to booking page
    header("Location: book_flight.php");
    exit();
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Air DS - Home</title>
    <link rel="stylesheet" href="styles.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>
    <?php include('header.php'); ?>

    <main>
        <div class="form-container">
            <h2 class="form-title">Book Your Flight</h2>
            <form id="flightSearchForm" action="" method="post" onsubmit="return validateForm()">
                <div class="form-group">
                    <label for="departure_airport">Departure Airport</label>
                    <select id="departure_airport" name="departure_airport" required>
                        <option value="">Select departure airport</option>
                        <?php foreach($airports as $airport): ?>
                            <option value="<?= $airport['code'] ?>"><?= $airport['name'] ?> (<?= $airport['code'] ?>)</option>
                        <?php endforeach; ?>
                    </select>
                </div>

                <div class="form-group">
                    <label for="arrival_airport">Arrival Airport</label>
                    <select id="arrival_airport" name="arrival_airport" required>
                        <option value="">Select arrival airport</option>
                        <?php foreach($airports as $airport): ?>
                            <option value="<?= $airport['code'] ?>"><?= $airport['name'] ?> (<?= $airport['code'] ?>)</option>
                        <?php endforeach; ?>
                    </select>
                </div>

                <div class="form-group">
                    <label for="flight_date">Flight Date</label>
                    <input type="date" id="flight_date" name="flight_date" required min="<?= date('Y-m-d') ?>">
                </div>

                <div class="form-group">
                    <label for="passengers">Number of Passengers</label>
                    <input type="number" id="passengers" name="passengers" min="1" max="6" required value="1">
                </div>

                <button type="submit" class="button" id="bookButton" <?= $isLoggedIn ? '' : 'disabled' ?>>Book Now</button>
                <?php if(!$isLoggedIn): ?>
                    <p class="login-alert">You must be <a href="login.php">logged in</a> to book a flight.</p>
                <?php endif; ?>
            </form>
        </div>
    </main>

    <?php include('footer.php'); ?>

    <script>
        function validateForm() {
            const departureAirport = document.getElementById('departure_airport').value;
            const arrivalAirport = document.getElementById('arrival_airport').value;
            const flightDate = document.getElementById('flight_date').value;
            
            if (departureAirport === arrivalAirport && departureAirport !== '') {
                alert("Departure and arrival airports cannot be the same");
                return false;
            }
            
            // Check if flight date is in the future
            const today = new Date();
            const selectedDate = new Date(flightDate);
            if (selectedDate < today) {
                alert("Flight date must be in the future");
                return false;
            }
            
            return true;
        }
    </script>
</body>
</html>