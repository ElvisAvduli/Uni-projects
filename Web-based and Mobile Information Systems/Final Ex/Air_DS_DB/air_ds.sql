-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Εξυπηρετητής: 127.0.0.1
-- Χρόνος δημιουργίας: 22 Απρ 2025 στις 19:32:25
-- Έκδοση διακομιστή: 10.4.32-MariaDB
-- Έκδοση PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Βάση δεδομένων: `air_ds`
--

-- --------------------------------------------------------

--
-- Δομή πίνακα για τον πίνακα `airports`
--

CREATE TABLE `airports` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `code` varchar(3) NOT NULL,
  `latitude` decimal(10,8) NOT NULL,
  `longitude` decimal(11,8) NOT NULL,
  `tax` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Άδειασμα δεδομένων του πίνακα `airports`
--

INSERT INTO `airports` (`id`, `name`, `code`, `latitude`, `longitude`, `tax`) VALUES
(1, 'Athens International Airport \"Eleftherios Venizelos\"', 'ATH', 37.93722500, 23.94523800, 150.00),
(2, 'Paris Charles de Gaulle Airport', 'CDG', 49.00972400, 2.54777800, 200.00),
(3, 'Leonardo da Vinci Rome Fiumicino Airport', 'FCO', 41.81080000, 12.25090000, 150.00),
(4, 'Adolfo Suárez Madrid–Barajas Airport', 'MAD', 40.48950000, 3.56430000, 250.00),
(5, 'Larnaka International Airport', 'LCA', 34.87150000, 33.60770000, 150.00),
(6, 'Brussels Airport', 'BRU', 50.90020000, 4.48590000, 200.00);

-- --------------------------------------------------------

--
-- Δομή πίνακα για τον πίνακα `passengers`
--

CREATE TABLE `passengers` (
  `id` int(11) NOT NULL,
  `reservation_id` int(11) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `seat_number` varchar(5) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Άδειασμα δεδομένων του πίνακα `passengers`
--

INSERT INTO `passengers` (`id`, `reservation_id`, `first_name`, `last_name`, `seat_number`) VALUES
(11, 13, 'Elvis', 'Avduli', '2C'),
(12, 14, 'Elvis', 'Avduli', '1A');

-- --------------------------------------------------------

--
-- Δομή πίνακα για τον πίνακα `reservations`
--

CREATE TABLE `reservations` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `departure_airport_id` int(11) NOT NULL,
  `arrival_airport_id` int(11) NOT NULL,
  `flight_date` date NOT NULL,
  `total_cost` decimal(10,2) NOT NULL,
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Άδειασμα δεδομένων του πίνακα `reservations`
--

INSERT INTO `reservations` (`id`, `user_id`, `departure_airport_id`, `arrival_airport_id`, `flight_date`, `total_cost`, `created_at`) VALUES
(13, 6, 1, 4, '2025-05-01', 587.46, '2025-04-22 19:52:47'),
(14, 6, 1, 6, '2025-06-01', 580.12, '2025-04-22 20:16:38');

-- --------------------------------------------------------

--
-- Δομή πίνακα για τον πίνακα `seats`
--

CREATE TABLE `seats` (
  `id` int(11) NOT NULL,
  `flight_date` date NOT NULL,
  `seat_number` varchar(5) NOT NULL,
  `is_booked` tinyint(1) DEFAULT 0,
  `reservation_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Άδειασμα δεδομένων του πίνακα `seats`
--

INSERT INTO `seats` (`id`, `flight_date`, `seat_number`, `is_booked`, `reservation_id`) VALUES
(12, '2025-05-01', '2C', 1, 13),
(13, '2025-06-01', '1A', 1, 14);

-- --------------------------------------------------------

--
-- Δομή πίνακα για τον πίνακα `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Άδειασμα δεδομένων του πίνακα `users`
--

INSERT INTO `users` (`user_id`, `first_name`, `last_name`, `username`, `password`, `email`, `created_at`) VALUES
(6, 'Elvis', 'Avduli', 'dummy', '$2y$10$DMbTl116b51S.t0gdevAQeLUmiRmUMPL1JN3uef0Lp6.q8htrCTW.', 'e22001@unipi.gr', '2025-04-22 19:46:24');

--
-- Ευρετήρια για άχρηστους πίνακες
--

--
-- Ευρετήρια για πίνακα `airports`
--
ALTER TABLE `airports`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `code` (`code`);

--
-- Ευρετήρια για πίνακα `passengers`
--
ALTER TABLE `passengers`
  ADD PRIMARY KEY (`id`),
  ADD KEY `reservation_id` (`reservation_id`);

--
-- Ευρετήρια για πίνακα `reservations`
--
ALTER TABLE `reservations`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `departure_airport_id` (`departure_airport_id`),
  ADD KEY `arrival_airport_id` (`arrival_airport_id`);

--
-- Ευρετήρια για πίνακα `seats`
--
ALTER TABLE `seats`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `flight_date` (`flight_date`,`seat_number`),
  ADD KEY `reservation_id` (`reservation_id`);

--
-- Ευρετήρια για πίνακα `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT για άχρηστους πίνακες
--

--
-- AUTO_INCREMENT για πίνακα `airports`
--
ALTER TABLE `airports`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT για πίνακα `passengers`
--
ALTER TABLE `passengers`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT για πίνακα `reservations`
--
ALTER TABLE `reservations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT για πίνακα `seats`
--
ALTER TABLE `seats`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT για πίνακα `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- Περιορισμοί για άχρηστους πίνακες
--

--
-- Περιορισμοί για πίνακα `passengers`
--
ALTER TABLE `passengers`
  ADD CONSTRAINT `passengers_ibfk_1` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`id`);

--
-- Περιορισμοί για πίνακα `reservations`
--
ALTER TABLE `reservations`
  ADD CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  ADD CONSTRAINT `reservations_ibfk_2` FOREIGN KEY (`departure_airport_id`) REFERENCES `airports` (`id`),
  ADD CONSTRAINT `reservations_ibfk_3` FOREIGN KEY (`arrival_airport_id`) REFERENCES `airports` (`id`);

--
-- Περιορισμοί για πίνακα `seats`
--
ALTER TABLE `seats`
  ADD CONSTRAINT `seats_ibfk_1` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
