def calculate_total_delay(packet_size, bit_rate, propagation_delay, bit_error_probability, timeout):

    if not (0 <= bit_error_probability < 1):  # Validate that bep is strictly between 0 and 1
        raise ValueError("Bit error probability must be between 0 and 1 (exclusive).")

    # Transmission delay = Packet size / Bit rate
    transmission_delay = packet_size / bit_rate

    # Probability of a successful transmission = (1 - bep)^P
    success_probability = (1 - bit_error_probability) ** packet_size

    # Handle extremely small or zero success probabilities
    if success_probability <= 1e-15:  # A threshold for practical purposes
        print("Warning: Bit error probability is too high for successful transmission. The expected number of transmissions is excessively large.")

        # Set success_probability to a small value to avoid division by zero
        success_probability = 1e-15

    # Expected number of transmissions = 1 / success_probability
    expected_transmissions = 1 / success_probability

    # Total delay calculation
    D_total = (transmission_delay + propagation_delay) * expected_transmissions

    # Add timeout for retransmissions in case of failure
    if expected_transmissions > 1:
        D_total += timeout * (expected_transmissions - 1)

    return D_total


# Function to get valid input from the user
def get_valid_input(prompt, cast_func, condition, error_message):
    while True:
        try:
            value = cast_func(input(prompt))
            if not condition(value):
                raise ValueError(error_message)
            return value
        except ValueError as e:
            print(e)

# Input from the user
packet_size = get_valid_input(
    "Enter the packet size (P) in bits: ",
    int,
    lambda x: x > 0,
    "Packet size must be greater than 0."
)

bit_rate = get_valid_input(
    "Enter the bit rate (C) in bits per second: ",
    int,
    lambda x: x > 0,
    "Bit rate must be greater than 0."
)

propagation_delay = get_valid_input(
    "Enter the propagation delay (PROP) in seconds: ",
    float,
    lambda x: x >= 0,
    "Propagation delay cannot be negative."
)

bit_error_probability = get_valid_input(
    "Enter the bit error probability (bep): ",
    float,
    lambda x: 0 <= x <= 1,
    "Bit error probability must be between 0 and 1."
)

timeout = get_valid_input(
    "Enter the timeout value (TIMEOUT) in seconds: ",
    float,
    lambda x: x > 0,
    "Timeout must be greater than 0."
)

# Calculate the total delay
total_delay = calculate_total_delay(packet_size, bit_rate, propagation_delay, bit_error_probability, timeout)

# Output the result
print(f"The total delay (D_total) is: {total_delay:.6f} seconds")
