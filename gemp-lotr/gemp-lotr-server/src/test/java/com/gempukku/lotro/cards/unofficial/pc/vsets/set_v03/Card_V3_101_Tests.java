package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_101_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("reach1", "103_101");
					put("reach2", "103_101");
					put("sauron", "103_100");     // Sauron, Lord of All Middle-earth: fierce only beside 6 companions
					put("oldsauron", "9_48");     // Sauron, The Lord of the Rings: damage +2, enduring, fierce
					put("mouth", "12_118");       // The Mouth of Sauron, Lieutenant of Barad-dur: [sauron] Man, vitality 3; an assignment ability
					put("swordsman", "3_98");     // Orc Swordsman: [sauron] Orc, vitality 3; stronger while he bears a weapon
					put("cirith", "10_81");       // Cirith Ungol Guard: [sauron] Uruk-hai, vitality 2; damage +1 and nothing else
					put("hunter", "1_256");       // Morgul Hunter: [sauron] Orc, vitality 4; stronger for each companion
					put("slayer", "3_93");        // Morgul Slayer: [sauron] Orc, vitality 2; a regroup ability
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 and nothing else
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, vitality 3; no text
					put("scimitar", "1_180");     // Goblin Scimitar: Shadow possession a [moria] Orc bears
					put("prisoners1", "103_99");  // Release the Prisoners!: [sauron] condition, support area
					put("prisoners2", "103_99");
					put("hollowing", "3_54");     // Hollowing of Isengard: [isengard] condition, support area; a Shadow ability
					put("throne", "17_105");      // Throne of the Dark Lord: [sauron] artifact, support area
					put("poison", "8_28");        // Spider Poison: [gollum] possession, support area until transferred
					put("breath1", "1_207");      // Black Breath: [wraith] condition, support area until transferred to a character
					put("breath2", "1_207");
					put("breath3", "1_207");
					put("lordofmoria", "1_21");   // Lord of Moria: [dwarven] condition, support area; each Dwarf is damage +1
					put("shards", "3_44");        // The Shards of Narsil: [gondor] artifact, support area; fellowship abilities
					put("toby", "1_305");         // Old Toby: [shire] possession, support area; a draw when played
					put("elrond", "1_40");        // Elrond, Lord of Rivendell: [elven] ally
					put("vilya", "3_27");         // Vilya: [elven] artifact Elrond bears; a maneuver ability of the Free Peoples player's
					put("sword", "1_299");        // Hobbit Sword: possession a Hobbit bears
					put("coat", "2_105");         // Mithril-coat: artifact Frodo bears
					put("knight", "5_35");        // Gondorian Knight: a [gondor] card to stack on the Shards
					put("aragorn", "1_89");       // Aragorn, Ranger of the North
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SauronsReachStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Sauron's Reach
		 * Unique: false
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert 3 [sauron] minions (or spot Sauron) to hinder all Free Peoples support cards.  For each card
		 * 		hindered, hinder a Shadow support card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("reach1");

		assertEquals("Sauron's Reach", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsThreeSauronMinionsToHinderEveryFreePeoplesSupportCardAndAsManyShadowSupportCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reach = scn.GetShadowCard("reach1");
		var mouth = scn.GetShadowCard("mouth");
		var swordsman = scn.GetShadowCard("swordsman");
		var cirith = scn.GetShadowCard("cirith");
		var hunter = scn.GetShadowCard("hunter");
		var slayer = scn.GetShadowCard("slayer");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var scimitar = scn.GetShadowCard("scimitar");
		var prisoners1 = scn.GetShadowCard("prisoners1");
		var prisoners2 = scn.GetShadowCard("prisoners2");
		var hollowing = scn.GetShadowCard("hollowing");
		var throne = scn.GetShadowCard("throne");
		var poison = scn.GetShadowCard("poison");
		var breath1 = scn.GetShadowCard("breath1");
		var breath2 = scn.GetShadowCard("breath2");
		var breath3 = scn.GetShadowCard("breath3");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var shards = scn.GetFreepsCard("shards");
		var toby = scn.GetFreepsCard("toby");
		var elrond = scn.GetFreepsCard("elrond");
		var vilya = scn.GetFreepsCard("vilya");
		var sword = scn.GetFreepsCard("sword");
		var coat = scn.GetFreepsCard("coat");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn);
		// In the Free Peoples support area: a condition, an artifact, a possession, and an ally bearing an artifact.
		scn.MoveCardsToSupportArea(lordofmoria, shards, toby);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);
		// Not there: a possession and an artifact the Ring-bearer bears, and a card stacked on the Shards.
		scn.AttachCardsTo(frodo, sword, coat);
		scn.StackCardsOn(shards, knight);
		// In the Shadow support area: two conditions and an artifact of three cultures and a possession; one more
		// condition borne by the ally there; and a sixth, hindered beforehand.
		scn.MoveCardsToSupportArea(prisoners1, prisoners2, hollowing, throne, poison, breath1);
		scn.AttachCardsTo(elrond, breath2);
		// Not there: a Shadow condition a companion bears.
		scn.AttachCardsTo(aragorn, breath3);
		scn.MoveCardsToHand(reach);

		scn.StartGame();
		scn.HinderCard(prisoners2);
		scn.MoveMinionsToTable(mouth, swordsman, cirith, hunter, slayer, savage, troop);
		// Nor a Shadow possession a minion bears.
		scn.AttachCardsTo(troop, scimitar);
		// Vitality 2 with 1 wound: the Slayer is exhausted.
		scn.AddWoundsToChar(slayer, 1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(reach));
		assertEquals(0, scn.GetWoundsOn(mouth));
		assertEquals(0, scn.GetWoundsOn(swordsman));
		assertEquals(0, scn.GetWoundsOn(cirith));
		assertEquals(0, scn.GetWoundsOn(hunter));
		assertFalse(scn.IsHindered(lordofmoria));
		assertFalse(scn.IsHindered(shards));
		assertFalse(scn.IsHindered(toby));
		assertFalse(scn.IsHindered(elrond));
		assertFalse(scn.IsHindered(vilya));

		scn.ShadowPlayCard(reach);

		// The Mouth of Sauron is no Sauron: three are to exert. A [sauron] Man, two [sauron] Orcs and a [sauron]
		// Uruk-hai may; an [isengard] Uruk-hai, a [moria] Orc and an exhausted [sauron] Orc may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mouth, swordsman, cirith, hunter));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, troop, slayer));
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(mouth, swordsman, cirith);

		assertEquals(1, scn.GetWoundsOn(mouth));
		assertEquals(1, scn.GetWoundsOn(swordsman));
		assertEquals(1, scn.GetWoundsOn(cirith));
		assertEquals(0, scn.GetWoundsOn(hunter));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(slayer));

		// Every Free Peoples card in the support area is hindered, the ally and what he bears included.
		assertTrue(scn.IsHindered(lordofmoria));
		assertTrue(scn.IsHindered(shards));
		assertTrue(scn.IsHindered(toby));
		assertTrue(scn.IsHindered(elrond));
		assertTrue(scn.IsHindered(vilya));
		assertFalse(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(coat));
		assertFalse(scn.IsHindered(knight));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(aragorn));

		// 5 cards were hindered, so 5 Shadow support cards are owed, of 6 that can be hindered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(prisoners1, hollowing, throne, poison, breath1, breath2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(prisoners2, breath3, scimitar, troop, swordsman, lordofmoria, sword));
		assertEquals(5, scn.ShadowGetChoiceMin());
		assertEquals(5, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(prisoners1, throne, poison, breath1, breath2);

		assertTrue(scn.IsHindered(prisoners1));
		assertTrue(scn.IsHindered(throne));
		assertTrue(scn.IsHindered(poison));
		assertTrue(scn.IsHindered(breath1));
		assertTrue(scn.IsHindered(breath2));
		assertFalse(scn.IsHindered(hollowing));
		assertFalse(scn.IsHindered(breath3));
		assertFalse(scn.IsHindered(scimitar));
		assertFalse(scn.IsHindered(troop));
		assertFalse(scn.IsHindered(hunter));
		// 5 set - 2 for the event
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.DISCARD, reach);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithSauronSpottedNoMinionExertsAndTwoCardsHinderedCostTwoShadowSupportCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reach = scn.GetShadowCard("reach1");
		var sauron = scn.GetShadowCard("sauron");
		var swordsman = scn.GetShadowCard("swordsman");
		var cirith = scn.GetShadowCard("cirith");
		var hunter = scn.GetShadowCard("hunter");
		var prisoners1 = scn.GetShadowCard("prisoners1");
		var hollowing = scn.GetShadowCard("hollowing");
		var throne = scn.GetShadowCard("throne");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var shards = scn.GetFreepsCard("shards");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(lordofmoria, shards);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToSupportArea(prisoners1, hollowing, throne);
		scn.MoveCardsToHand(reach);

		scn.StartGame();
		// Three [sauron] minions able to exert stand beside Sauron.
		scn.MoveMinionsToTable(sauron, swordsman, cirith, hunter);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(reach));
		assertFalse(scn.IsHindered(lordofmoria));
		assertFalse(scn.IsHindered(shards));

		scn.ShadowPlayCard(reach);

		// No minion is asked for: the next thing chosen is the Shadow support cards.
		assertEquals(0, scn.GetWoundsOn(sauron));
		assertEquals(0, scn.GetWoundsOn(swordsman));
		assertEquals(0, scn.GetWoundsOn(cirith));
		assertEquals(0, scn.GetWoundsOn(hunter));
		assertTrue(scn.IsHindered(lordofmoria));
		assertTrue(scn.IsHindered(shards));
		assertFalse(scn.IsHindered(sword));
		assertTrue(scn.ShadowHasCardChoiceAvailable(prisoners1, hollowing, throne));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sauron, swordsman, cirith, hunter));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(hollowing, throne);

		assertTrue(scn.IsHindered(hollowing));
		assertTrue(scn.IsHindered(throne));
		assertFalse(scn.IsHindered(prisoners1));
		assertEquals(0, scn.GetWoundsOn(sauron));
		assertEquals(0, scn.GetWoundsOn(swordsman));
		assertInZone(Zone.DISCARD, reach);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithSauronAloneItIsPlayedInTheManeuverPhaseOnlyAndHindersNoMoreShadowSupportCardsThanThereAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reach1 = scn.GetShadowCard("reach1");
		var reach2 = scn.GetShadowCard("reach2");
		var oldsauron = scn.GetShadowCard("oldsauron");
		var prisoners1 = scn.GetShadowCard("prisoners1");
		var throne = scn.GetShadowCard("throne");
		var breath3 = scn.GetShadowCard("breath3");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var shards = scn.GetFreepsCard("shards");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(lordofmoria, shards);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(prisoners1, throne);
		scn.AttachCardsTo(aragorn, breath3);
		scn.MoveCardsToHand(reach1, reach2);

		scn.StartGame();
		scn.MoveMinionsToTable(oldsauron);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 2 companions + 2 for the site
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(reach1));

		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(reach1));
		assertEquals(0, scn.GetWoundsOn(oldsauron));
		assertFalse(scn.IsHindered(lordofmoria));
		assertFalse(scn.IsHindered(shards));
		assertFalse(scn.IsHindered(elrond));
		assertFalse(scn.IsHindered(prisoners1));
		assertFalse(scn.IsHindered(throne));

		scn.ShadowPlayCard(reach1);

		assertEquals(12, scn.GetTwilight());
		assertInZone(Zone.DISCARD, reach1);
		assertEquals(0, scn.GetWoundsOn(oldsauron));
		assertTrue(scn.IsHindered(lordofmoria));
		assertTrue(scn.IsHindered(shards));
		assertTrue(scn.IsHindered(elrond));
		// 3 cards were hindered and there are 2 Shadow support cards: both, with nothing to choose.
		assertTrue(scn.IsHindered(prisoners1));
		assertTrue(scn.IsHindered(throne));
		assertFalse(scn.IsHindered(breath3));
		assertFalse(scn.IsHindered(oldsauron));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(reach2));
		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(reach2));
	}

	@Test
	public void WithNoFreePeoplesSupportCardToHinderTheExertionsArePaidAndNoShadowSupportCardIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reach = scn.GetShadowCard("reach1");
		var swordsman = scn.GetShadowCard("swordsman");
		var cirith = scn.GetShadowCard("cirith");
		var hunter = scn.GetShadowCard("hunter");
		var savage = scn.GetShadowCard("savage");
		var prisoners1 = scn.GetShadowCard("prisoners1");
		var throne = scn.GetShadowCard("throne");
		var frodo = scn.GetRingBearer();
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var sword = scn.GetFreepsCard("sword");
		// The one Free Peoples support card is hindered already; the Sword is borne by a companion.
		scn.MoveCardsToSupportArea(lordofmoria);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToSupportArea(prisoners1, throne);
		scn.MoveCardsToHand(reach);

		scn.StartGame();
		scn.HinderCard(lordofmoria);
		scn.MoveMinionsToTable(swordsman, cirith, hunter, savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(lordofmoria));
		assertTrue(scn.ShadowPlayAvailable(reach));
		assertEquals(0, scn.GetWoundsOn(swordsman));
		assertEquals(0, scn.GetWoundsOn(cirith));
		assertEquals(0, scn.GetWoundsOn(hunter));

		scn.ShadowPlayCard(reach);

		// The three [sauron] minions there are exert without a choice.
		assertEquals(1, scn.GetWoundsOn(swordsman));
		assertEquals(1, scn.GetWoundsOn(cirith));
		assertEquals(1, scn.GetWoundsOn(hunter));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.DISCARD, reach);
		assertTrue(scn.IsHindered(lordofmoria));
		assertFalse(scn.IsHindered(sword));
		// No card was hindered by the event, so none is owed and none is asked for.
		assertFalse(scn.IsHindered(prisoners1));
		assertFalse(scn.IsHindered(throne));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutSauronUntilAThirdSauronMinionIsAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reach = scn.GetShadowCard("reach1");
		var mouth = scn.GetShadowCard("mouth");
		var swordsman = scn.GetShadowCard("swordsman");
		var slayer = scn.GetShadowCard("slayer");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		scn.MoveCardsToSupportArea(lordofmoria);
		scn.MoveCardsToHand(reach);

		scn.StartGame();
		// The Mouth of Sauron and an Orc Swordsman can exert; the third [sauron] minion is exhausted (vitality 2,
		// 1 wound); an [isengard] Uruk-hai and a [moria] Orc could exert and do not count.
		scn.MoveMinionsToTable(mouth, swordsman, slayer, savage, troop);
		scn.AddWoundsToChar(slayer, 1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(reach));

		scn.RemoveWoundsFromChar(slayer, 1);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(reach));
		assertFalse(scn.IsHindered(lordofmoria));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
