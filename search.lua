local keywords = {"laptop", "phone", "tv", "camera", "shoes"}

request = function()
   local keyword = keywords[math.random(1, #keywords)]
   local path = "/api/products/search?keyword=" .. keyword .. "&page=100&size=20"
   return wrk.format("GET", path)
end
