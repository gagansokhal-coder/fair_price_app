FROM golang:1.21-alpine

WORKDIR /app

# Copy module files first for caching
COPY go.mod go.sum ./
RUN go mod download

# Copy source
COPY . .

# Build
RUN go build -o server .

EXPOSE 8080

CMD ["./server"]
