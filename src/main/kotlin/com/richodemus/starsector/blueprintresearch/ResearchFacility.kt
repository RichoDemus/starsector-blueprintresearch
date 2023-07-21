package com.richodemus.starsector.blueprintresearch

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignClockAPI
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Items
import com.fs.starfarer.api.impl.campaign.ids.Submarkets
import com.fs.starfarer.api.loading.FighterWingSpecAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import com.fs.starfarer.api.util.WeightedRandomPicker
import lunalib.lunaSettings.LunaSettings
import org.lazywizard.lazylib.ext.logging.i
import java.util.*


class ResearchFacility: BaseIndustry() {
//    var lastDayChecked:Int = Global.getSector().clock.day
    override fun apply() {
        super.apply(true);

        demand(Commodities.SHIPS, 2)
        demand(Commodities.RARE_METALS, 2)
        demand(Commodities.VOLATILES, 1)
    }

//    override fun advance(amount: Float) {
//        super.advance(amount);
//        val clock: CampaignClockAPI = Global.getSector().clock
//
//        if (lastDayChecked == clock.day) {
//            return
//        }
//
//        lastDayChecked = clock.day
//
//    }

    override fun generateCargoForGatheringPoint(random: Random?): CargoAPI {
        val shipBlueprintsToAdd = if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            LunaSettings.getInt("richodemus-blueprintresearch", "shipBluePrintsToAdd")?:1
        } else {
            1
        }
        val weaponBlueprintsToAdd = if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            LunaSettings.getInt("richodemus-blueprintresearch", "weaponBluePrintsToAdd")?:1
        } else {
            1
        }
        val fighterBlueprintsToAdd = if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            LunaSettings.getInt("richodemus-blueprintresearch", "fighterBluePrintsToAdd")?:1
        } else {
            1
        }

        val ships = getShipsWithBlueprints().toMutableList()
        val knownShips = Global.getSector().playerFaction.knownShips
        val bpsInStorage = getBlueprintsInStorage().shipBluePrints
        val unknownShips = ships.filter { !knownShips.contains(it.hullId) }.filter { !bpsInStorage.contains(it.hullId) }
        Global.getLogger(javaClass).i({ "Total ${ships.size} ships, ${knownShips.size} known, ${bpsInStorage.size} bps in storage, for a total of ${unknownShips.size} unknown ships" }, null)
        val picker = WeightedRandomPicker<ShipHullSpecAPI>(random)
        for (spec in unknownShips) {
            picker.add(spec, 1f * spec.rarity)
        }
        val result = Global.getFactory().createCargo(true)
        val shipBpsToAdd = mutableListOf<String>()
        for (i in 1..shipBlueprintsToAdd) {
            val pick: ShipHullSpecAPI? = picker.pickAndRemove()
            if (pick != null) {
                shipBpsToAdd.add(pick.hullId)
            }
        }
        Global.getLogger(javaClass).i({ "Adding ship bps: $shipBpsToAdd" }, null)
        for (ship in shipBpsToAdd) {
            result.addSpecial(SpecialItemData(Items.SHIP_BP, ship), 1f)
        }

        val specs = Global.getSettings().allWeaponSpecs
        val bpTags = specs.flatMap { it.tags }.filter { it.endsWith("_bp") }

        val (weaponsWithBlueprints, weaponsWithoutBlueprints) = specs.partition { it.tags.any { it in bpTags } }
        val knownWeapons = Global.getSector().playerFaction.knownWeapons
        val wpnBpsInStorage = getBlueprintsInStorage().weaponBluePrints
        val unknownWeapons = weaponsWithBlueprints.filter { !knownWeapons.contains(it.weaponId) }.filter { !wpnBpsInStorage.contains(it.weaponId) }
        Global.getLogger(javaClass).i({ "Total ${weaponsWithBlueprints.size} weapons, ${knownWeapons.size} known, ${wpnBpsInStorage.size} bps in storage, for a total of ${unknownWeapons.size} unknown weapons" }, null)

        val picker2 = WeightedRandomPicker<WeaponSpecAPI>(random)
        for (spec in unknownWeapons) {
            picker2.add(spec, 1f * spec.rarity)
        }
        val wpnBpsToAdd = mutableListOf<String>()
        for (i in 1..weaponBlueprintsToAdd) {
            val pick2 = picker2.pickAndRemove()
            if (pick2 != null) {
                wpnBpsToAdd.add(pick2.weaponId)
            }
        }
        Global.getLogger(javaClass).i({ "Adding weapon bps: $wpnBpsToAdd" }, null)
        for (wpn in wpnBpsToAdd) {
            result.addSpecial(SpecialItemData(Items.WEAPON_BP, wpn), 1f)
        }

        val fighterSpecs = Global.getSettings().allFighterWingSpecs
        val fighterBpTags = fighterSpecs.flatMap { it.tags }.filter { it.endsWith("_bp") }

        val (fighterWithBlueprints, fighterWithoutBlueprints) = fighterSpecs.partition { it.tags.any { it in fighterBpTags } }
        val knownfighter = Global.getSector().playerFaction.knownFighters
        val fighterBpsInStorage = getBlueprintsInStorage().fighterBlueprints
        val unknownfighter = fighterWithBlueprints.filter { !knownfighter.contains(it.id) }.filter { !fighterBpsInStorage.contains(it.id) }
        Global.getLogger(javaClass).i({ "Total ${fighterWithBlueprints.size} fighter, ${knownfighter.size} known, ${fighterBpsInStorage.size} bps in storage, for a total of ${unknownfighter.size} unknown fighter" }, null)

        val picker3 = WeightedRandomPicker<FighterWingSpecAPI>(random)
        for (spec in unknownfighter) {
            picker3.add(spec, 1f * spec.rarity)
        }
        val fighterBpsToAdd = mutableListOf<String>()
        for (i in 1..fighterBlueprintsToAdd) {
            val pick3 = picker3.pickAndRemove()
            if (pick3 != null) {
                fighterBpsToAdd.add(pick3.id)

            }
        }
        Global.getLogger(javaClass).i({ "Adding fighter bps: $fighterBpsToAdd" }, null)
        for (fighter in fighterBpsToAdd) {
            result.addSpecial(SpecialItemData(Items.FIGHTER_BP, fighter), 1f)
        }

        return result
    }

    fun getShipsWithBlueprints(): List<ShipHullSpecAPI> {
        val specs = Global.getSettings().allShipHullSpecs
        val bpTags = specs.flatMap { it.tags }.filter { it.endsWith("_bp") }

        val (shipsWithBlueprints, shipsWithoutBlueprints) = specs.partition { it.tags.any { it in bpTags } }

        return shipsWithBlueprints
    }

    data class Blueprints(val shipBluePrints:Set<String>, val weaponBluePrints:Set<String>, val fighterBlueprints:Set<String>)

    fun getBlueprintsInStorage(): Blueprints {
        val storage = market.getSubmarket(Submarkets.SUBMARKET_STORAGE)

        val cargo: CargoAPI = storage.cargo
        val copy = cargo.createCopy()
        val stacks = copy.stacksCopy
        val shipBps = mutableListOf<String>()
        val weaponBps = mutableListOf<String>()
        val fighterBps = mutableListOf<String>()
        for (stack in stacks) {
            val data = stack.data //contains the nice stuff
            if (data is SpecialItemData) {
                if (data.id == "ship_bp") {
                    shipBps.add(data.data)
                } else if (data.id == "weapon_bp") {
                    weaponBps.add(data.data)
                } else if (data.id == "fighter_bp") {
                    fighterBps.add(data.data)
                }
            }
        }
        return Blueprints(shipBps.toHashSet(), weaponBps.toHashSet(), fighterBps.toHashSet())
    }
}
