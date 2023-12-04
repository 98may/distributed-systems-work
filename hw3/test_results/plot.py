import pandas as pd
import matplotlib.pyplot as plt

# Load data
CLIENT_LOG_PATH = "/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw3/test_results";

data = pd.read_csv(CLIENT_LOG_PATH+'/java_30_throughputs.csv')

# Create a scatter plot
plt.plot(data['Time(s)'], data['Throughput(req/s)'], marker='o', linestyle='-', markersize=2)

# Add labels and title
plt.xlabel('Time (s)')
plt.ylabel('Throughput (req/s)')
plt.title('Throughput Over Time' +' of LoadTester 10 30 2 java 2 w/ MQ')

# Calculate metrics
mean_throughput = data['Throughput(req/s)'].mean()
median_throughput = data['Throughput(req/s)'].median()
p99_throughput = data['Throughput(req/s)'].quantile(0.99)
min_throughput = data['Throughput(req/s)'].min()
max_throughput = data['Throughput(req/s)'].max()

# Add text annotation for the metrics
metrics_text = (
    f"Mean: {mean_throughput:.2f} req/s\n"
    f"Median: {median_throughput:.2f} req/s\n"
    f"P99: {p99_throughput:.2f} req/s\n"
    f"Min: {min_throughput:.2f} req/s\n"
    f"Max: {max_throughput:.2f} req/s\n"
)

# Adjust the subplot's position to create space for the text on the right
plt.subplots_adjust(right=0.7)  # Adjust the right edge of the subplot

# Use plt.text() with figure coordinates (using the `transform` parameter)
# plt.text(x=1.05, y=0.5, s=metrics_text, transform=plt.gcf().transFigure, ha='left', va='center', fontsize=9, bbox=dict(facecolor='none', edgecolor='black', boxstyle='round,pad=0.5'))
plt.text(1.02, 0.5, metrics_text, transform=plt.gca().transAxes, fontsize=9, ha='left', va='center',
         bbox=dict(facecolor='none', edgecolor='black', boxstyle='round,pad=0.5'))

output_image_path = CLIENT_LOG_PATH+'/java_30_throughputs.png'
plt.savefig(output_image_path, format='png', dpi=300)


# Show plot
plt.show()

