wrk.method = "GET"

-- List of random keywords to search for
local keywords = {
    "laptop", "phone", "monitor", "keyboard", "mouse",
    "tablet", "headphones", "camera", "smartwatch", "speaker",
    "desk", "chair", "cable", "adapter", "charger",
    "apple", "samsung", "dell", "hp", "lenovo"
}

-- Initialize the random seed
math.randomseed(os.time())

request = function()
    -- Pick a random keyword from the list
    local keyword = keywords[math.random(#keywords)]
    
    -- Pick a random page (0 to 5) to simulate real user pagination
    local page = math.random(0, 5)
    
    -- Construct the dynamic path
    local path = "/api/products/search?keyword=" .. keyword .. "&page=" .. page .. "&size=20"
    
    -- Return the formatted HTTP request
    return wrk.format(nil, path)
end
