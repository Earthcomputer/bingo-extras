gamemode survival @a
$bingo start --time-limit 1d --require-client --shape $(shape) --size $(size) --difficulty $(difficulty) --continue-after-win red orange blue green yellow pink purple black white
$execute if predicate bingo:$(consistent) run bingospreadplayers4d 0 0 200 @a[gamemode=!spectator] #bingo:excluded
$execute unless predicate bingo:$(consistent) run bingospreadplayers 0 0 1000000 30000 $(respect_teams) @a[gamemode=!spectator] $(same_biomes) #bingo:excluded
execute as @a[gamemode=!spectator] run clearspawnpoint
$execute at @p[gamemode=!spectator,team=red] run function bingo:init_team {team: "red", bonus_chest: "$(bonus_chest)", seed: $(seed), consistent: "$(consistent)"}
$execute at @p[gamemode=!spectator,team=orange] run function bingo:init_team {team: "orange", bonus_chest: "$(bonus_chest)", seed: $(seed), consistent: "$(consistent)"}
$execute at @p[gamemode=!spectator,team=green] run function bingo:init_team {team: "green", bonus_chest: "$(bonus_chest)", seed: $(seed), consistent: "$(consistent)"}
$execute at @p[gamemode=!spectator,team=blue] run function bingo:init_team {team: "blue", bonus_chest: "$(bonus_chest)", seed: $(seed), consistent: "$(consistent)"}
$execute at @p[gamemode=!spectator,team=yellow] run function bingo:init_team {team: "yellow", bonus_chest: "$(bonus_chest)", seed: $(seed), consistent: "$(consistent)"}
$execute at @p[gamemode=!spectator,team=pink] run function bingo:init_team {team: "pink", bonus_chest: "$(bonus_chest)", seed: $(seed), consistent: "$(consistent)"}
$execute at @p[gamemode=!spectator,team=purple] run function bingo:init_team {team: "purple", bonus_chest: "$(bonus_chest)", seed: $(seed), consistent: "$(consistent)"}
$execute at @p[gamemode=!spectator,team=black] run function bingo:init_team {team: "black", bonus_chest: "$(bonus_chest)", seed: $(seed), consistent: "$(consistent)"}
$execute at @p[gamemode=!spectator,team=white] run function bingo:init_team {team: "white", bonus_chest: "$(bonus_chest)", seed: $(seed), consistent: "$(consistent)"}
time set 0
weather rain 1
clear @a[gamemode=!spectator]
xp set @a[gamemode=!spectator] 0
xp set @a[gamemode=!spectator] 0 levels
effect clear @a[gamemode=!spectator]
advancement revoke @a[gamemode=!spectator] everything
difficulty hard
setentityval health @a[gamemode=!spectator] 20
setentityval food @a[gamemode=!spectator] 20
setentityval saturation @a[gamemode=!spectator] 5
setentityval exhaustion @a[gamemode=!spectator] 0
setstat @a[gamemode=!spectator] custom minecraft:time_since_rest 0