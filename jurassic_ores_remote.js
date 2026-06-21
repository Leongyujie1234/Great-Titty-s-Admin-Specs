WorldgenEvents.modify(event => {
    event.modifyFeature('jurassicreborn:amber_ore_placed', feature => {
        feature.setCount(1).setChance(0.05)
    })
    event.modifyFeature('jurassicreborn:ice_shard_ore_placed', feature => {
        feature.setCount(1).setChance(0.05)
    })
    event.modifyFeature('jurassicreborn:gypsum_stone_placed', feature => {
        feature.setCount(1).setChance(0.05)
    })
    event.modifyFeature('jurassicreborn:flora_fossil_placed', feature => {
        feature.setCount(1).setChance(0.05)
    })
})
