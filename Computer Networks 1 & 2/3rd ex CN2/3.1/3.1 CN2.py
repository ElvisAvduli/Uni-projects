def crc_remainder(data, generator):
  """Calculates the CRC remainder for given data and generator polynomial."""
  data_len = len(data)
  generator_len = len(generator)

  # Append zeros to the data
  data += '0' * (generator_len - 1)

  for i in range(data_len):
    if data[i] == '1':
      for j in range(generator_len):
        data = data[:i+j] + str(int(data[i+j]) ^ int(generator[j])) + data[i+j+1:]

  return data[data_len:]

def simulate_error(data, position):
  """Simulates an error in the data at the given position."""
  if position >= 0 and position < len(data):
      data = list(data)  # convert to mutable list
      data[position] = '1' if data[position] == '0' else '0'
      data = "".join(data) # convert back to string
  return data

# Define the generator polynomial G(x) = x^3 + 1
generator = "1001"

while True:
    data_packet = input("Enter the data packet (12 bits): ")
    # Check if the data_packet is 12 characters long and contains only '0' or '1'
    if len(data_packet) != 12 or not all(bit in '01' for bit in data_packet):
        print("Invalid data packet. Please enter a 12-bit binary data packet (only '0' and '1').")
    else:
        break

# Transmitter side
remainder = crc_remainder(data_packet, generator)
transmitted_packet = data_packet + remainder
print("Transmitted packet:", transmitted_packet)

# Simulate error (optional)
error_position = 2  # Introduce an error at bit position 2 (0-based indexing)

#Uncomment the following line to introduce an error
transmitted_packet = simulate_error(transmitted_packet, error_position)

#Receiver side
received_remainder = crc_remainder(transmitted_packet, generator)
print("Received Remainder:", received_remainder)

if received_remainder == "000":
  print("No error detected. Data accepted.")
  # Pass the original data packet to the network layer
  print("Data passed to the network layer:", data_packet)
else:
  print("Error detected. Data rejected.")