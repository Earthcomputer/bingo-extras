bingo reset
execute store result storage bingo:storage root.seed long 1 run random value 1..2147483647 bingo:seeds
data modify storage bingo:storage root merge from storage bingo:vars
schedule function bingo:setup_inner_wrapper 2t