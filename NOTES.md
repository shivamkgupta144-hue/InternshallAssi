# Debugging Notes

## Bug 1 – Incorrect Search and Status Filtering

### Location
backend/src/main/java/com/internal/tasktracker/TaskRepository.java

### Discovery
While testing the API with search and status filters, I found that tasks with incorrect statuses were sometimes returned.

### Root Cause
The SQL query used AND and OR without proper parentheses. AND has higher precedence than OR, so the status condition was not consistently applied to all search results.

### Fix
I grouped the title and description search conditions using parentheses so that the search condition is evaluated first, followed by the status filter.

### Verification
I tested `/api/tasks?status=DONE`. After the fix, only DONE tasks were returned.

### Impact
This was a high-value correctness bug because users could see tasks that did not match their selected status.


## Bug 2 – Invalid Status Handling

### Location
backend/src/main/java/com/internal/tasktracker/TaskController.java

### Discovery
I tested the API with an invalid status using `/api/tasks?status=INVALID`.

### Root Cause
The code used `TaskStatus.valueOf()` directly. For an unknown status, it throws `IllegalArgumentException`, resulting in a server error.

### Fix
I added validation and exception handling around status conversion. Invalid status values now return 400 Bad Request with a meaningful error message.

### Verification
The INVALID status request now returns 400 Bad Request instead of an unhandled server error.

### Impact
The API now handles invalid user input safely and provides a meaningful client error.


## Bug 3 – Invalid Pagination Parameters

### Location
backend/src/main/java/com/internal/tasktracker/TaskController.java

### Discovery
I tested invalid pagination values such as `page=0` and `pageSize=-5`.

### Root Cause
The API did not validate pagination parameters before calculating list indexes. Invalid values could cause incorrect pagination or runtime errors.

### Fix
I added validation:
- page must be at least 1
- pageSize must be between 1 and 100

### Verification
Requests using `page=0` or `pageSize=-5` now return 400 Bad Request with a validation message.

### Impact
This prevents invalid input from reaching pagination logic and makes the API more robust.


## Improvement 4 – Unnecessary Request Delay

### Location
backend/src/main/java/com/internal/tasktracker/TaskController.java

### Discovery
While reviewing the API request flow, I found an artificial `Thread.sleep()` based on the search query length.

### Root Cause
The request thread was unnecessarily blocked before executing the database query. This added unnecessary latency to API requests.

### Fix
I removed the artificial `Thread.sleep()` logic so requests can reach the database immediately.

### Impact
This improves response time and avoids unnecessarily blocking server threads.

### Note
I treated this as a performance improvement rather than a functional correctness bug.


## Testing

I verified:
- Search
- Status filtering
- Pagination
- Invalid status
- Invalid pagination
- Normal API response
- Frontend smoke tests

## Assumptions and Scope

I focused on high-value, reproducible issues and avoided speculative changes that could not be confirmed from the application's behavior or source code.

I prioritized correctness and user impact and kept unrelated working functionality unchanged.