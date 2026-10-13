package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_115_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "103_115");
					put("merry", "1_302");        // Merry, Friend to Sam: a Hobbit, resistance 6; a skirmish ability never used here
					put("pippin", "1_306");       // Pippin, Friend to Frodo: his text is about [shire] tales
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a maneuver ability never used here
					put("bounder", "1_286");      // Bounder: a Hobbit ally; a skirmish ability

					// Shadow conditions that lie in the support area, of four costs and three cultures.
					put("wreathed", "1_238");     // Wreathed in Shadow: [wraith], cost 0; an archery ability
					put("armory", "1_157");       // Uruk-hai Armory: [isengard], cost 1; needs an Uruk-hai
					put("fewnumber", "1_260");    // The Number Must Be Few: [sauron], cost 2; needs 7 companions
					put("bloodlust", "1_144");    // Uruk Bloodlust: [isengard], cost 3; a response to an Uruk-hai's win
					// Weary: an [isengard] condition, cost 1, borne by a companion or an ally; it answers its bearer being
					// assigned to an [isengard] tracker.
					put("weary1", "4_212");
					put("weary2", "4_212");
					// Throw Yourself in Next Time: a [moria] condition, cost 0, borne by a Hobbit companion, who may then
					// be exerted only by Shadow cards.
					put("throwyourself", "2_71");

					// Shadow possessions that lie in the support area.
					put("hides", "4_19");         // Hides: [dunland], cost 1; a response for [dunland] Men
					put("grond", "8_103");        // Grond: [sauron], cost 3; Shadow and regroup abilities
					put("ships", "8_65");         // Ships of Great Draught: [raider], cost 2; Shadow and regroup abilities
					put("sails", "8_50");         // Black Sails of Umbar: [raider], cost 2; a Shadow ability
					// Goblin Scimitar: a [moria] possession, cost 0, borne by a [moria] Orc.
					put("scimitar", "1_180");
					// Goblin Spear: a [moria] possession, cost 1, borne by a [moria] minion; it answers its bearer killing
					// a companion.
					put("spear", "1_182");

					// Shadow artifacts that lie in the support area: neither condition nor possession.
					put("palantir", "3_67");      // The Palantir of Orthanc: [isengard], cost 0; a Shadow ability
					put("crown", "101_39");       // Pale Crown: [wraith], cost 1; a skirmish ability

					// Free Peoples cards that lie in the support area.
					put("map", "1_318");          // Thror's Map: a possession, cost 0; fellowship or regroup ability
					put("toby", "1_305");         // Old Toby: a possession, cost 1
					put("longstem", "17_21");     // Long-stemmed Pipe: a condition, cost 2; a regroup ability
					put("halfling", "17_106");    // Halfling Leaf: a condition, cost 1; a skirmish ability

					put("troop", "1_177");        // Goblin Patrol Troop: a [moria] Orc with no text, cost 6
					put("runner1", "1_178");      // Goblin Runner: strength 5; its text is about being played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli, Bearer of Grudges 9_4 bears the Ring: resistance 4, and +1 for his damage +1.
	protected VirtualTableScenario GetGimliRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "103_115");
					put("frodo", "13_149");     // Frodo, Frenzied Fighter: the one Frodo who cannot bear the Ring, so the one who may be in play beside another Ring-bearer (Q-0159); resistance 8, and his texts want more minions than companions, or a skirmish he is not in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Sam, Undaunted
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: Ring-bound. Frodo is resistance +2.
		 * 		Maneuver: If you cannot spot more than 4 companions, add a burden to choose condition or possession,
		 * 		then choose odd or even.  Hinder all Shadow support cards with a type and twilight cost matching your
		 * 		choice. Limit once per turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sam");

		assertEquals("Sam", card.getBlueprint().getTitle());
		assertEquals("Undaunted", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void PlayedForTwoAndFrodoIsThenResistancePlusTwoAndNoOtherHobbitIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(sam);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(merry));

		scn.FreepsPlayCard(sam);

		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(2, scn.GetTwilight());
		assertEquals(12, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(merry));
		assertEquals(6, scn.GetResistance(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AFrodoWhoIsNotTheRingBearerIsResistancePlusTwoAndARingBearerWhoIsNotFrodoIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliRingBearerScenario();

		var gimli = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetFreepsCard("frodo");

		scn.MoveCompanionsToTable(frodo);

		scn.StartGame();

		assertEquals(8, scn.GetResistance(frodo));
		// 4, and 1 for his damage +1
		assertEquals(5, scn.GetResistance(gimli));

		scn.MoveCompanionsToTable(sam);

		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(gimli));
		assertEquals(6, scn.GetResistance(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ConditionAndEvenHindersEveryShadowSupportConditionOfEvenCostAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var map = scn.GetFreepsCard("map");
		var toby = scn.GetFreepsCard("toby");
		var longstem = scn.GetFreepsCard("longstem");
		var halfling = scn.GetFreepsCard("halfling");
		var wreathed = scn.GetShadowCard("wreathed");
		var armory = scn.GetShadowCard("armory");
		var fewnumber = scn.GetShadowCard("fewnumber");
		var bloodlust = scn.GetShadowCard("bloodlust");
		var wearyOnAlly = scn.GetShadowCard("weary1");
		var wearyOnCompanion = scn.GetShadowCard("weary2");
		var hides = scn.GetShadowCard("hides");
		var grond = scn.GetShadowCard("grond");
		var ships = scn.GetShadowCard("ships");
		var sails = scn.GetShadowCard("sails");
		var scimitar = scn.GetShadowCard("scimitar");
		var palantir = scn.GetShadowCard("palantir");
		var crown = scn.GetShadowCard("crown");
		var throwOnCompanion = scn.GetShadowCard("throwyourself");
		var spear = scn.GetShadowCard("spear");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner1");

		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(map, toby, longstem, halfling);
		scn.MoveCardsToSupportArea(wreathed, armory, fewnumber, bloodlust, hides, grond, ships, sails, palantir, crown);
		scn.AttachCardsTo(bounder, wearyOnAlly);
		scn.AttachCardsTo(merry, wearyOnCompanion, throwOnCompanion);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.AttachCardsTo(troop, scimitar);
		scn.AttachCardsTo(runner, spear);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.IsHindered(wreathed));
		assertFalse(scn.IsHindered(fewnumber));

		scn.FreepsUseCardAction(sam);

		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.FreepsChoiceAvailable("conditions"));
		assertTrue(scn.FreepsChoiceAvailable("possessions"));
		scn.FreepsChoose("conditions");
		assertTrue(scn.FreepsChoiceAvailable("odd-numbered"));
		assertTrue(scn.FreepsChoiceAvailable("even-numbered"));
		scn.FreepsChoose("even-numbered");

		// Costs 0 and 2.
		assertTrue(scn.IsHindered(wreathed));
		assertTrue(scn.IsHindered(fewnumber));
		// Conditions of odd cost, in the support area and on an ally.
		assertFalse(scn.IsHindered(armory));
		assertFalse(scn.IsHindered(bloodlust));
		assertFalse(scn.IsHindered(wearyOnAlly));
		assertFalse(scn.IsHindered(wearyOnCompanion));
		// A condition of cost 0 that a companion bears is no support card.
		assertFalse(scn.IsHindered(throwOnCompanion));
		// Possessions and artifacts of even cost: 2, 2, 0 on a minion, 0.
		assertFalse(scn.IsHindered(ships));
		assertFalse(scn.IsHindered(sails));
		assertFalse(scn.IsHindered(scimitar));
		assertFalse(scn.IsHindered(palantir));
		// A Free Peoples condition of cost 2.
		assertFalse(scn.IsHindered(longstem));
		assertFalse(scn.IsHindered(hides));
		assertFalse(scn.IsHindered(grond));
		assertFalse(scn.IsHindered(crown));
		assertFalse(scn.IsHindered(map));
		assertFalse(scn.IsHindered(toby));
		assertFalse(scn.IsHindered(halfling));
		assertFalse(scn.IsHindered(spear));
		assertFalse(scn.IsHindered(troop));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ConditionAndOddHindersEveryShadowSupportConditionOfOddCostTheOneOnAnAllyIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var map = scn.GetFreepsCard("map");
		var toby = scn.GetFreepsCard("toby");
		var longstem = scn.GetFreepsCard("longstem");
		var halfling = scn.GetFreepsCard("halfling");
		var wreathed = scn.GetShadowCard("wreathed");
		var armory = scn.GetShadowCard("armory");
		var fewnumber = scn.GetShadowCard("fewnumber");
		var bloodlust = scn.GetShadowCard("bloodlust");
		var wearyOnAlly = scn.GetShadowCard("weary1");
		var wearyOnCompanion = scn.GetShadowCard("weary2");
		var hides = scn.GetShadowCard("hides");
		var grond = scn.GetShadowCard("grond");
		var ships = scn.GetShadowCard("ships");
		var sails = scn.GetShadowCard("sails");
		var scimitar = scn.GetShadowCard("scimitar");
		var palantir = scn.GetShadowCard("palantir");
		var crown = scn.GetShadowCard("crown");
		var throwOnCompanion = scn.GetShadowCard("throwyourself");
		var spear = scn.GetShadowCard("spear");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner1");

		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(map, toby, longstem, halfling);
		scn.MoveCardsToSupportArea(wreathed, armory, fewnumber, bloodlust, hides, grond, ships, sails, palantir, crown);
		scn.AttachCardsTo(bounder, wearyOnAlly);
		scn.AttachCardsTo(merry, wearyOnCompanion, throwOnCompanion);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.AttachCardsTo(troop, scimitar);
		scn.AttachCardsTo(runner, spear);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.IsHindered(armory));
		assertFalse(scn.IsHindered(wearyOnAlly));

		scn.FreepsUseCardAction(sam);
		scn.FreepsChoose("conditions");
		scn.FreepsChoose("odd-numbered");

		// Costs 1 and 3 in the support area, and the Weary an ally bears: what an ally bears is in the support area.
		assertTrue(scn.IsHindered(armory));
		assertTrue(scn.IsHindered(bloodlust));
		assertTrue(scn.IsHindered(wearyOnAlly));
		// The same card on a companion is no support card.
		assertFalse(scn.IsHindered(wearyOnCompanion));
		// Conditions of even cost.
		assertFalse(scn.IsHindered(wreathed));
		assertFalse(scn.IsHindered(fewnumber));
		// Possessions and an artifact of odd cost: 1, 3, 1.
		assertFalse(scn.IsHindered(hides));
		assertFalse(scn.IsHindered(grond));
		assertFalse(scn.IsHindered(crown));
		// A Free Peoples condition of cost 1.
		assertFalse(scn.IsHindered(halfling));
		assertFalse(scn.IsHindered(ships));
		assertFalse(scn.IsHindered(sails));
		assertFalse(scn.IsHindered(scimitar));
		assertFalse(scn.IsHindered(palantir));
		assertFalse(scn.IsHindered(map));
		assertFalse(scn.IsHindered(toby));
		assertFalse(scn.IsHindered(longstem));
		assertFalse(scn.IsHindered(bounder));
		assertFalse(scn.IsHindered(throwOnCompanion));
		assertFalse(scn.IsHindered(spear));
		assertFalse(scn.IsHindered(troop));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void PossessionAndEvenHindersEveryShadowSupportPossessionOfEvenCostAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var map = scn.GetFreepsCard("map");
		var toby = scn.GetFreepsCard("toby");
		var longstem = scn.GetFreepsCard("longstem");
		var halfling = scn.GetFreepsCard("halfling");
		var wreathed = scn.GetShadowCard("wreathed");
		var armory = scn.GetShadowCard("armory");
		var fewnumber = scn.GetShadowCard("fewnumber");
		var bloodlust = scn.GetShadowCard("bloodlust");
		var wearyOnAlly = scn.GetShadowCard("weary1");
		var wearyOnCompanion = scn.GetShadowCard("weary2");
		var hides = scn.GetShadowCard("hides");
		var grond = scn.GetShadowCard("grond");
		var ships = scn.GetShadowCard("ships");
		var sails = scn.GetShadowCard("sails");
		var scimitar = scn.GetShadowCard("scimitar");
		var palantir = scn.GetShadowCard("palantir");
		var crown = scn.GetShadowCard("crown");
		var throwOnCompanion = scn.GetShadowCard("throwyourself");
		var spear = scn.GetShadowCard("spear");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner1");

		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(map, toby, longstem, halfling);
		scn.MoveCardsToSupportArea(wreathed, armory, fewnumber, bloodlust, hides, grond, ships, sails, palantir, crown);
		scn.AttachCardsTo(bounder, wearyOnAlly);
		scn.AttachCardsTo(merry, wearyOnCompanion, throwOnCompanion);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.AttachCardsTo(troop, scimitar);
		scn.AttachCardsTo(runner, spear);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.IsHindered(ships));
		assertFalse(scn.IsHindered(sails));

		scn.FreepsUseCardAction(sam);
		scn.FreepsChoose("possessions");
		assertTrue(scn.FreepsChoiceAvailable("odd-numbered"));
		assertTrue(scn.FreepsChoiceAvailable("even-numbered"));
		scn.FreepsChoose("even-numbered");

		// Both of cost 2.
		assertTrue(scn.IsHindered(ships));
		assertTrue(scn.IsHindered(sails));
		// A possession of cost 0 that a minion bears is no support card.
		assertFalse(scn.IsHindered(scimitar));
		// An artifact of cost 0 is no possession.
		assertFalse(scn.IsHindered(palantir));
		// Possessions of odd cost.
		assertFalse(scn.IsHindered(hides));
		assertFalse(scn.IsHindered(grond));
		// Conditions of even cost.
		assertFalse(scn.IsHindered(wreathed));
		assertFalse(scn.IsHindered(fewnumber));
		// A Free Peoples possession of cost 0.
		assertFalse(scn.IsHindered(map));
		assertFalse(scn.IsHindered(armory));
		assertFalse(scn.IsHindered(bloodlust));
		assertFalse(scn.IsHindered(wearyOnAlly));
		assertFalse(scn.IsHindered(wearyOnCompanion));
		assertFalse(scn.IsHindered(crown));
		assertFalse(scn.IsHindered(toby));
		assertFalse(scn.IsHindered(longstem));
		assertFalse(scn.IsHindered(halfling));
		assertFalse(scn.IsHindered(throwOnCompanion));
		assertFalse(scn.IsHindered(spear));
		assertFalse(scn.IsHindered(troop));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void PossessionAndOddHindersEveryShadowSupportPossessionOfOddCostAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var map = scn.GetFreepsCard("map");
		var toby = scn.GetFreepsCard("toby");
		var longstem = scn.GetFreepsCard("longstem");
		var halfling = scn.GetFreepsCard("halfling");
		var wreathed = scn.GetShadowCard("wreathed");
		var armory = scn.GetShadowCard("armory");
		var fewnumber = scn.GetShadowCard("fewnumber");
		var bloodlust = scn.GetShadowCard("bloodlust");
		var wearyOnAlly = scn.GetShadowCard("weary1");
		var wearyOnCompanion = scn.GetShadowCard("weary2");
		var hides = scn.GetShadowCard("hides");
		var grond = scn.GetShadowCard("grond");
		var ships = scn.GetShadowCard("ships");
		var sails = scn.GetShadowCard("sails");
		var scimitar = scn.GetShadowCard("scimitar");
		var palantir = scn.GetShadowCard("palantir");
		var crown = scn.GetShadowCard("crown");
		var throwOnCompanion = scn.GetShadowCard("throwyourself");
		var spear = scn.GetShadowCard("spear");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner1");

		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(map, toby, longstem, halfling);
		scn.MoveCardsToSupportArea(wreathed, armory, fewnumber, bloodlust, hides, grond, ships, sails, palantir, crown);
		scn.AttachCardsTo(bounder, wearyOnAlly);
		scn.AttachCardsTo(merry, wearyOnCompanion, throwOnCompanion);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.AttachCardsTo(troop, scimitar);
		scn.AttachCardsTo(runner, spear);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.IsHindered(hides));
		assertFalse(scn.IsHindered(grond));

		scn.FreepsUseCardAction(sam);
		scn.FreepsChoose("possessions");
		scn.FreepsChoose("odd-numbered");

		// Costs 1 and 3.
		assertTrue(scn.IsHindered(hides));
		assertTrue(scn.IsHindered(grond));
		// A possession of cost 1 that a minion bears is no support card.
		assertFalse(scn.IsHindered(spear));
		// An artifact of cost 1 is no possession.
		assertFalse(scn.IsHindered(crown));
		// Possessions of even cost.
		assertFalse(scn.IsHindered(ships));
		assertFalse(scn.IsHindered(sails));
		assertFalse(scn.IsHindered(scimitar));
		// Conditions of odd cost.
		assertFalse(scn.IsHindered(armory));
		assertFalse(scn.IsHindered(bloodlust));
		assertFalse(scn.IsHindered(wearyOnAlly));
		assertFalse(scn.IsHindered(wearyOnCompanion));
		// A Free Peoples possession of cost 1.
		assertFalse(scn.IsHindered(toby));
		assertFalse(scn.IsHindered(wreathed));
		assertFalse(scn.IsHindered(fewnumber));
		assertFalse(scn.IsHindered(palantir));
		assertFalse(scn.IsHindered(map));
		assertFalse(scn.IsHindered(longstem));
		assertFalse(scn.IsHindered(halfling));
		assertFalse(scn.IsHindered(throwOnCompanion));
		assertFalse(scn.IsHindered(troop));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithFiveCompanionsToSpotAndOfferedWithFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var wreathed = scn.GetShadowCard("wreathed");
		var troop = scn.GetShadowCard("troop");

		// Five companions with Frodo, and an ally.
		scn.MoveCompanionsToTable(sam, merry, pippin, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(wreathed);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sam));

		// A hindered companion cannot be spotted: four are left, and the ally is no companion.
		scn.HinderCard(aragorn);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsActionAvailable(sam));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheRingBearerIsHinderedAndNoBurdenCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var wreathed = scn.GetShadowCard("wreathed");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(wreathed);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(sam));

		scn.HinderCard(frodo);
		scn.FreepsRefreshDecision();

		assertFalse(scn.FreepsActionAvailable(sam));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.IsHindered(wreathed));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoShadowConditionOrPossessionInASupportAreaTheBurdenIsAddedAndNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var halfling = scn.GetFreepsCard("halfling");
		var toby = scn.GetFreepsCard("toby");
		var wearyOnCompanion = scn.GetShadowCard("weary2");
		var scimitar = scn.GetShadowCard("scimitar");
		var palantir = scn.GetShadowCard("palantir");
		var troop = scn.GetShadowCard("troop");

		// A Shadow condition on a companion, a Shadow possession on a minion, a Shadow artifact and Free Peoples
		// cards in the support areas: nothing that either answer to either question could hinder.
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveCardsToSupportArea(halfling, toby);
		scn.MoveCardsToSupportArea(palantir);
		scn.AttachCardsTo(merry, wearyOnCompanion);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, scimitar);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetBurdens());
		// It has a cost, so it is offered though it can hinder nothing.
		assertTrue(scn.FreepsActionAvailable(sam));

		scn.FreepsUseCardAction(sam);

		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.IsHindered(wearyOnCompanion));
		assertFalse(scn.IsHindered(scimitar));
		assertFalse(scn.IsHindered(palantir));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoShadowSupportCardOfTheTypeChosenOddOrEvenIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var toby = scn.GetFreepsCard("toby");
		var wreathed = scn.GetShadowCard("wreathed");
		var scimitar = scn.GetShadowCard("scimitar");
		var troop = scn.GetShadowCard("troop");

		// A Shadow condition in the support area. The only Shadow possession is on a minion, and the only
		// possession in a support area is a Free Peoples one.
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(toby);
		scn.MoveCardsToSupportArea(wreathed);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, scimitar);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(sam);

		// The first question is a real one: there is a condition to hinder.
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.FreepsChoiceAvailable("conditions"));
		assertTrue(scn.FreepsChoiceAvailable("possessions"));
		scn.FreepsChoose("possessions");

		// Neither odd nor even could hinder a possession.
		assertFalse(scn.IsHindered(wreathed));
		assertFalse(scn.IsHindered(scimitar));
		assertFalse(scn.IsHindered(toby));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void UsedOnceItIsWithheldForTheRestOfTheTurnAndOfferedAgainOnTheNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var wreathed = scn.GetShadowCard("wreathed");
		var armory = scn.GetShadowCard("armory");
		var fewnumber = scn.GetShadowCard("fewnumber");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveCardsToSupportArea(wreathed, armory, fewnumber);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(sam));

		scn.FreepsUseCardAction(sam);
		scn.FreepsChoose("conditions");
		scn.FreepsChoose("even-numbered");

		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.IsHindered(wreathed));
		assertTrue(scn.IsHindered(fewnumber));
		assertFalse(scn.IsHindered(armory));

		// Later in the same maneuver phase: a burden could still be added and the Armory could still be hindered.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sam));

		// The fellowship moves again: a second maneuver phase in the same turn.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.IsHindered(armory));
		assertFalse(scn.FreepsActionAvailable(sam));
		assertEquals(1, scn.GetBurdens());

		// The other player's turn, and then this player's next.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.MoveMinionsToTable(runner2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(sam));

		scn.FreepsUseCardAction(sam);
		scn.FreepsChoose("conditions");
		scn.FreepsChoose("odd-numbered");

		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.IsHindered(armory));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
