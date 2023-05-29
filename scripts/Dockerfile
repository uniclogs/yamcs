FROM python:3.9-slim-buster

EXPOSE 10010
EXPOSE 10026
EXPOSE 10027

# Set the working directory
WORKDIR /app

# Set the PYTHONUNBUFFERED environment variable to disable output buffering
ENV PYTHONUNBUFFERED=1

# Copy the mock_satellite.py script to the working directory
COPY mock_satellite.py .
COPY requirements.txt .

# Install the required packages
RUN pip install -r requirements.txt

# Set the entrypoint to run the mock_satellite.py script
ENTRYPOINT ["python", "mock_satellite.py"]
