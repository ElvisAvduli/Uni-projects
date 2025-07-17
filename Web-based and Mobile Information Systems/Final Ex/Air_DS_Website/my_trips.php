<?php
session_start();
require_once('db_connection.php');

// Check if user is logged in
if (!isset($_SESSION['user_id'])) {
    header("Location: login.php");
    exit();
}

// Check for booking success message
$booking_success = false;
if (isset($_SESSION['booking_success'])) {
    $booking_success = true;
    unset($_SESSION['booking_success']);
}

// Get user's reservations with airport information
$stmt = $conn->prepare("
    SELECT r.id as reservation_id,
           r.user_id,
           r.departure_airport_id,
           r.arrival_airport_id,
           r.flight_date,
           r.total_cost,
           r.created_at,
           d.name as departure_name, 
           d.code as departure_code,
           d.tax as departure_tax,
           a.name as arrival_name, 
           a.code as arrival_code,
           a.tax as arrival_tax
    FROM reservations r
    JOIN airports d ON r.departure_airport_id = d.id
    JOIN airports a ON r.arrival_airport_id = a.id
    WHERE r.user_id = ?
    ORDER BY r.flight_date ASC, r.created_at DESC
");
$stmt->execute([$_SESSION['user_id']]);
$reservations = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Get passengers for each reservation
foreach ($reservations as &$reservation) {
    $stmt = $conn->prepare("
        SELECT first_name, last_name, seat_number 
        FROM passengers 
        WHERE reservation_id = ?
    ");
    $stmt->execute([$reservation['reservation_id']]);
    $reservation['passengers'] = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Calculate flight cost (total minus taxes)
    $reservation['flight_cost'] = $reservation['total_cost'] - 
                                 ($reservation['departure_tax'] + $reservation['arrival_tax']);
}
unset($reservation); // Break the reference

// Handle cancellation
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['cancel_trip'])) {
    $reservation_id = $_POST['reservation_id'];
    
    // Check if cancellation is allowed (at least 30 days in the future)
    $stmt = $conn->prepare("SELECT flight_date FROM reservations WHERE id = ? AND user_id = ?");
    $stmt->execute([$reservation_id, $_SESSION['user_id']]);
    $reservation = $stmt->fetch();
    
    if ($reservation) {
        $flight_date = new DateTime($reservation['flight_date']);
        $today = new DateTime();
        $today->setTime(0, 0, 0);
        $interval = $today->diff($flight_date);
        
        if ($interval->days >= 30 && $interval->invert == 0) {
            // Delete reservation and associated records
            $conn->beginTransaction();
            try {
                // First delete seats
                $stmt = $conn->prepare("DELETE FROM seats WHERE reservation_id = ?");
                $stmt->execute([$reservation_id]);
                
                // Then delete passengers
                $stmt = $conn->prepare("DELETE FROM passengers WHERE reservation_id = ?");
                $stmt->execute([$reservation_id]);
                
                // Finally delete reservation
                $stmt = $conn->prepare("DELETE FROM reservations WHERE id = ? AND user_id = ?");
                $stmt->execute([$reservation_id, $_SESSION['user_id']]);
                
                $conn->commit();
                
                // Refresh page
                header("Location: my_trips.php");
                exit();
            } catch (Exception $e) {
                $conn->rollBack();
                $error = "Error cancelling trip: " . $e->getMessage();
            }
        } else {
            $error = "You can only cancel trips at least 30 days in advance.";
        }
    } else {
        $error = "Reservation not found or you don't have permission to cancel it.";
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Air DS - My Trips</title>
    <link rel="stylesheet" href="styles.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>
    <?php include('header.php'); ?>
    
    <main>
        <?php if ($booking_success): ?>
            <div class="success-message" style="background-color: #eeffee; color: #008800; padding: 1rem; margin-bottom: 1rem; border-radius: 4px;">
                Your booking was successful!
            </div>
        <?php endif; ?>
        
        <?php if (isset($error)): ?>
            <div class="error-message" style="background-color: #ffeeee; color: #cc0000; padding: 1rem; margin-bottom: 1rem; border-radius: 4px;">
                <?= $error ?>
            </div>
        <?php endif; ?>
        
        <h2>My Trips</h2>
        
        <?php if (empty($reservations)): ?>
            <p>You don't have any upcoming trips.</p>
        <?php else: ?>
            <div class="trip-list">
                <?php foreach ($reservations as $reservation): ?>
                    <div class="trip-card">
                        <div class="trip-header">
                            <h3><?= $reservation['departure_name'] ?> (<?= $reservation['departure_code'] ?>) → <?= $reservation['arrival_name'] ?> (<?= $reservation['arrival_code'] ?>)</h3>
                            <span><?= date('F j, Y', strtotime($reservation['flight_date'])) ?></span>
                        </div>
                        
                        <div class="trip-content">
                            <div>
                                <h4>Passengers</h4>
                                <ul>
                                    <?php foreach ($reservation['passengers'] as $passenger): ?>
                                        <li><?= $passenger['first_name'] ?> <?= $passenger['last_name'] ?> (Seat: <?= $passenger['seat_number'] ?>)</li>
                                    <?php endforeach; ?>
                                </ul>
                            </div>
                            
                            <div>
                                <h4>Cost Details</h4>
                                <p>Departure Tax: €<?= number_format($reservation['departure_tax'], 2) ?></p>
                                <p>Arrival Tax: €<?= number_format($reservation['arrival_tax'], 2) ?></p>
                                <p>Flight Cost: €<?= number_format($reservation['flight_cost'], 2) ?></p>
                                <p><strong>Total: €<?= number_format($reservation['total_cost'], 2) ?></strong></p>
                                <p><small>Booked on: <?= date('F j, Y H:i', strtotime($reservation['created_at'])) ?></small></p>
                                
                                <?php 
                                // Check if cancellation is allowed (at least 30 days in the future)
                                $flight_date = new DateTime($reservation['flight_date']);
                                $today = new DateTime();
                                $today->setTime(0, 0, 0);
                                $interval = $today->diff($flight_date);
                                $can_cancel = $interval->days >= 30 && $interval->invert == 0;
                                ?>
                                
                                <?php if ($can_cancel): ?>
                                    <form method="post" onsubmit="return confirm('Are you sure you want to cancel this trip? This action cannot be undone.');">
                                        <input type="hidden" name="reservation_id" value="<?= $reservation['reservation_id'] ?>">
                                        <button type="submit" name="cancel_trip" class="cancel-trip">Cancel Trip</button>
                                    </form>
                                <?php else: ?>
                                    <button class="cancel-trip" disabled>
                                        <?= $interval->invert == 1 ? 'Flight already occurred' : 'Can cancel until ' . date('F j, Y', strtotime('-30 days', strtotime($reservation['flight_date']))) ?>
                                    </button>
                                <?php endif; ?>
                            </div>
                        </div>
                    </div>
                <?php endforeach; ?>
            </div>
        <?php endif; ?>
    </main>
    
    <?php include('footer.php'); ?>
</body>
</html>