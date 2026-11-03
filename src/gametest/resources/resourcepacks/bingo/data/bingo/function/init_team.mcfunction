$teamspawnpoint $(team) ~ ~ ~
$execute if predicate bingo:$(bonus_chest) if predicate bingo:$(consistent) run placebonuschest ~ ~ ~ 10 minecraft:chests/spawn_bonus_chest $(seed)
$execute if predicate bingo:$(bonus_chest) unless predicate bingo:$(consistent) run placebonuschest ~ ~ ~ 10 minecraft:chests/spawn_bonus_chest