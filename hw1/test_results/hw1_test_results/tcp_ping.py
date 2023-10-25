import socket
import time

def tcp_ping(host, port):
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.settimeout(1)  # Set a timeout of 1 second
    start = time.time()
    try:
        s.connect((host, port))
    except socket.error as e:
        print(f"Connection error: {e}")
        return
    end = time.time()
    s.close()
    latency = (end - start) * 1000  # Convert to milliseconds
    print(f"TCP Ping Latency: {latency:.2f} ms")

# Replace 'your-ec2-instance-ip' with your EC2 instance IP address
# Replace 'your-port-number' with the port number you want to check
tcp_ping('3.80.33.155', 8081)
