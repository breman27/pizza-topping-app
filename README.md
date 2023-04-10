# pizza-topping-app

Delivering all your pizza metrics - hot!

## How to run

There is a dockerfile that you can use to run as a container, 
it will automatically kick the jar off when run, you just need to add the port that you'll be listening on.
Running the following will build the docker container and kick the job off. 

` docker build -t pizza-topping-app .`

` docker run -p 8080:8080 pizza-topping-app`

Alternatively you can just run a local instance of this in any IDE and it should spin up on your default tomcat port. 

## Features

This includes the ability to read from a selected API every 10 seconds by default. Settings can be tweaked 
on the cron setting of the collectToppingMetrics function. For now it reads from itself and the list of Topping objects start empty. 

New Topping objects, defined as an email: String and a list of toppings: List\<String\>, can be added by hitting the POST /toppings/post endpoint
with a json body of an `{"email": "string", "toppings": ["some", "toppings"]}`

Metrics included are a unique user count by topping, total count of toppings, most popular toppings, least popular toppings, and most popular combinations
of toppings. More information can be found below:

### Unique user count by topping:

`GET /toppings/unique-user-count`

### Get all toppings:

`GET /toppings/get-toppings`

### Total count of toppings

`GET /toppings/total-count`

### Update metrics (mainly used internally) updates the unique user count and total count by sending a list of toppings:

`POST /toppings/update-metrics`
Body is a list of Topping objects in json format (email, toppings)

### Most popular combinations of toppings:

`GET /toppings/most-popular-combo`
Optional parameter count can be added to return a list of popular combos. Default is ?count=1

### Most popular topping:

`GET /toppings/most-popular`
Optional parameter count can be added to return a list of popular toppings. Default is ?count=1

### Least popular toppings:

`GET /toppings/least-popular`
Optional parameter count can be added to return a list of least popular toppings. Default is ?count=1

### Post new data to list of toppings:

`POST /toppings/post`
Body is a list of Topping objects in json format (email, toppings)
