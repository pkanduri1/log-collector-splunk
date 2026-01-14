#!/bin/bash

# docker-run.sh - Build and run the Log Collector application with Docker
# This script builds and runs both frontend and backend containers

set -e  # Exit on error

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print colored messages
print_info() {
    echo -e "${BLUE}ℹ ${1}${NC}"
}

print_success() {
    echo -e "${GREEN}✓ ${1}${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ ${1}${NC}"
}

print_error() {
    echo -e "${RED}✗ ${1}${NC}"
}

# Function to display usage
usage() {
    cat << EOF
Usage: ./docker-run.sh [OPTIONS]

Build and run the Log Collector application using Docker Compose.

OPTIONS:
    -h, --help              Show this help message
    -b, --build             Force rebuild of images
    -d, --detach            Run in detached mode (background)
    -s, --stop              Stop and remove containers
    -l, --logs              Show logs from running containers
    -c, --clean             Clean up all containers, images, and volumes
    --no-cache              Build without using cache

EXAMPLES:
    ./docker-run.sh                    # Build and run (interactive mode)
    ./docker-run.sh -b                 # Force rebuild and run
    ./docker-run.sh -d                 # Run in background
    ./docker-run.sh -s                 # Stop containers
    ./docker-run.sh -l                 # View logs
    ./docker-run.sh -c                 # Clean up everything

ENVIRONMENT VARIABLES:
    OPENAI_API_KEY              OpenAI API key (required if using OpenAI)
    GOOGLE_AI_GEMINI_API_KEY    Google Gemini API key
    ANTHROPIC_API_KEY           Anthropic API key
    AI_PROVIDER                 AI provider to use (default: openai)
    SPLUNK_HOST                 Splunk host (default: host.docker.internal)
    SPLUNK_PORT                 Splunk port (default: 8089)
    SPLUNK_USERNAME             Splunk username (default: admin)
    SPLUNK_PASSWORD             Splunk password (default: changeme)

EOF
}

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    print_success "Docker is running"
}

# Function to check environment variables
check_env() {
    local ai_provider="${AI_PROVIDER:-openai}"
    
    print_info "AI Provider: ${ai_provider}"
    
    case "$ai_provider" in
        openai)
            if [ -z "$OPENAI_API_KEY" ]; then
                print_warning "OPENAI_API_KEY is not set"
                print_info "Set it with: export OPENAI_API_KEY='your-key-here'"
                read -p "Continue anyway? (y/n) " -n 1 -r
                echo
                if [[ ! $REPLY =~ ^[Yy]$ ]]; then
                    exit 1
                fi
            else
                print_success "OPENAI_API_KEY is set"
            fi
            ;;
        gemini)
            if [ -z "$GOOGLE_AI_GEMINI_API_KEY" ]; then
                print_warning "GOOGLE_AI_GEMINI_API_KEY is not set"
            else
                print_success "GOOGLE_AI_GEMINI_API_KEY is set"
            fi
            ;;
        anthropic)
            if [ -z "$ANTHROPIC_API_KEY" ]; then
                print_warning "ANTHROPIC_API_KEY is not set"
            else
                print_success "ANTHROPIC_API_KEY is set"
            fi
            ;;
        ollama)
            print_info "Using Ollama (no API key required)"
            ;;
    esac
}

# Function to build and run
build_and_run() {
    local build_flag=""
    local detach_flag=""
    local no_cache_flag=""
    
    if [ "$FORCE_BUILD" = true ]; then
        build_flag="--build"
    fi
    
    if [ "$DETACH" = true ]; then
        detach_flag="-d"
    fi
    
    if [ "$NO_CACHE" = true ]; then
        no_cache_flag="--no-cache"
    fi
    
    print_info "Building and starting containers..."
    
    if [ -n "$no_cache_flag" ]; then
        docker compose build $no_cache_flag
        docker compose up $detach_flag
    else
        docker compose up $build_flag $detach_flag
    fi
    
    if [ "$DETACH" = true ]; then
        print_success "Containers started in background"
        print_info "Frontend: http://localhost:5173"
        print_info "Backend:  http://localhost:8081"
        print_info "View logs with: ./docker-run.sh -l"
        print_info "Stop with:      ./docker-run.sh -s"
    fi
}

# Function to stop containers
stop_containers() {
    print_info "Stopping containers..."
    docker compose down
    print_success "Containers stopped"
}

# Function to show logs
show_logs() {
    print_info "Showing logs (Ctrl+C to exit)..."
    docker compose logs -f
}

# Function to clean up
clean_up() {
    print_warning "This will remove all containers, images, and volumes for this project"
    read -p "Are you sure? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_info "Cleaning up..."
        docker compose down -v --rmi all
        print_success "Cleanup complete"
    else
        print_info "Cleanup cancelled"
    fi
}

# Main script
main() {
    FORCE_BUILD=false
    DETACH=false
    NO_CACHE=false
    ACTION="run"
    
    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                usage
                exit 0
                ;;
            -b|--build)
                FORCE_BUILD=true
                shift
                ;;
            -d|--detach)
                DETACH=true
                shift
                ;;
            -s|--stop)
                ACTION="stop"
                shift
                ;;
            -l|--logs)
                ACTION="logs"
                shift
                ;;
            -c|--clean)
                ACTION="clean"
                shift
                ;;
            --no-cache)
                NO_CACHE=true
                FORCE_BUILD=true
                shift
                ;;
            *)
                print_error "Unknown option: $1"
                usage
                exit 1
                ;;
        esac
    done
    
    # Check Docker
    check_docker
    
    # Execute action
    case $ACTION in
        run)
            check_env
            build_and_run
            ;;
        stop)
            stop_containers
            ;;
        logs)
            show_logs
            ;;
        clean)
            clean_up
            ;;
    esac
}

# Run main function
main "$@"
