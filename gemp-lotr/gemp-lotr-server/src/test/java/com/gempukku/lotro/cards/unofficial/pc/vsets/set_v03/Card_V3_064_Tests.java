package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_064_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fear", "103_64");
					put("fear2", "103_64");
					put("sky1", "103_97");        // Ominous Sky: a [sauron] twilight condition, cost 2, nothing needed to play it; its texts answer the play of an Orc or Troll and cost (2) in the Shadow or regroup phase
					put("sky2", "103_97");
					put("gloom", "103_65");       // Cover of Darkness, Omen of Gloom: a [ringwraith] twilight condition
					put("servants", "1_219");     // The Nine Servants of Sauron: a [ringwraith] condition that is not twilight
					put("nelya", "2_84");         // Ulaire Nelya, Ringwraith in Twilight: a twilight card that is no condition
					put("squealer1", "8_77");     // Morgul Squealer: a [ringwraith] Orc; Shadow-phase abilities only
					put("squealer2", "8_77");
					put("witchking", "8_84");     // The Witch-king, Black Captain: a [ringwraith] Nazgul; his text touches Men skirmishing him
					put("khamul", "103_61");      // Ulaire Attea, Khamul of the East: a Nazgul who is [raider], not [ringwraith]; his text needs 3 burdens
					put("troop", "1_177");        // Goblin Patrol Troop: a [moria] minion with no text
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: unbound
					put("gimli", "1_13");         // Gimli, Son of Gloin: unbound
					put("sam", "1_311");          // Sam, Son of Hamfast: Ring-bound, and not the Ring-bearer
					put("elrond", "1_40");        // Elrond, Lord of Rivendell: an ally
					put("steed", "1_208");        // Black Steed: a Shadow possession; bearer must be a Nazgul
					put("armor", "1_8");          // Dwarven Armor: a Free Peoples possession; bearer must be a Dwarf
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CoverOfDarknessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Cover of Darkness, Omen of Fear
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Twilight.
		 * 	To play, hinder 2 twilight conditions.
		 * 	Assignment: Spot 3 [ringwraith] minions, remove a burden, and hinder this condition to spot a
		 * 	[ringwraith] minion and an unbound companion.  Hinder all other characters.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fear");

		assertEquals("Cover of Darkness", card.getBlueprint().getTitle());
		assertEquals("Omen of Fear", card.getBlueprint().getSubtitle());
		// Two dots: no more than two copies in play
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayHindersTwoTwilightConditionsOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var fear2 = scn.GetShadowCard("fear2");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var servants = scn.GetShadowCard("servants");
		var nelya = scn.GetShadowCard("nelya");

		scn.MoveCardsToHand(fear);
		// Twilight conditions: two that are [sauron], and the other copy of this card, which is [ringwraith].
		// (A Cover of Darkness of another subtitle would be the third of that title, and bar the play.)
		// And a condition that is not twilight.
		scn.MoveCardsToSupportArea(sky1, sky2, fear2, servants);

		scn.StartGame();
		// A twilight card that is no condition
		scn.MoveMinionsToTable(nelya);
		scn.SetTwilight(8);
		scn.FreepsPass();

		// 8 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(fear));

		scn.ShadowPlayCard(fear);

		assertTrue(scn.ShadowHasCardChoiceAvailable(sky1, sky2, fear2));
		assertFalse(scn.ShadowHasCardChoiceAvailable(servants));
		assertFalse(scn.ShadowHasCardChoiceAvailable(nelya));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(fear2));

		scn.ShadowChooseCards(sky1, fear2);

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(fear2));
		assertFalse(scn.IsHindered(sky2));
		assertFalse(scn.IsHindered(servants));
		assertFalse(scn.IsHindered(nelya));
		// 11 - 2 to play
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, fear);
		assertFalse(scn.IsHindered(fear));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithFewerThanTwoTwilightConditionsToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var gloom = scn.GetShadowCard("gloom");
		var servants = scn.GetShadowCard("servants");
		var nelya = scn.GetShadowCard("nelya");

		scn.MoveCardsToHand(fear, sky2);
		// One twilight condition that can be hindered. Beside it: one that is hindered already, a condition
		// that is not twilight, and (below) a twilight minion.
		scn.MoveCardsToSupportArea(sky1, gloom, servants);
		scn.HinderCard(gloom);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.SetTwilight(8);
		scn.FreepsPass();

		// 8 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(11, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(fear));

		// A second twilight condition, played from hand
		scn.ShadowPlayCard(sky2);
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(fear));
		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(sky2));

		// With exactly two there is nothing to choose.
		scn.ShadowPlayCard(fear);

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky2));
		assertFalse(scn.IsHindered(servants));
		assertFalse(scn.IsHindered(nelya));
		// 9 - 2 to play
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, fear);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AssignmentActionLeavesOneRingwraithMinionAndOneUnboundCompanionAndHindersEveryOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var fear = scn.GetShadowCard("fear");
		var fear2 = scn.GetShadowCard("fear2");
		var squealer1 = scn.GetShadowCard("squealer1");
		var squealer2 = scn.GetShadowCard("squealer2");
		var witchking = scn.GetShadowCard("witchking");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var elrond = scn.GetFreepsCard("elrond");

		scn.MoveCardsToSupportArea(fear, fear2);
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		// Exactly three [ringwraith] minions, a Nazgul and two Orcs, and a Nazgul of another culture
		scn.MoveMinionsToTable(squealer1, squealer2, witchking, khamul);
		scn.AddBurdens(2);

		// Not an archery action
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fear));
		scn.ShadowPass();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.FreepsPass();
		assertEquals(2, scn.GetBurdens());
		assertFalse(scn.IsHindered(fear));
		assertFalse(scn.IsHindered(squealer1));
		assertFalse(scn.IsHindered(squealer2));
		assertFalse(scn.IsHindered(khamul));
		assertFalse(scn.IsHindered(gimli));
		assertFalse(scn.IsHindered(sam));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(elrond));
		assertTrue(scn.ShadowActionAvailable(fear));
		assertTrue(scn.ShadowActionAvailable(fear2));

		scn.ShadowUseCardAction(fear);

		assertTrue(scn.ShadowHasCardChoiceAvailable(squealer1, squealer2, witchking));
		assertFalse(scn.ShadowHasCardChoiceAvailable(khamul));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(witchking);

		// Unbound companions: not the Ring-bearer, not Sam, not the ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertFalse(scn.ShadowHasCardChoiceAvailable(frodo));
		assertFalse(scn.ShadowHasCardChoiceAvailable(sam));
		assertFalse(scn.ShadowHasCardChoiceAvailable(elrond));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.IsHindered(fear));

		assertFalse(scn.IsHindered(witchking));
		assertFalse(scn.IsHindered(aragorn));
		// Every other character, whoever's it is and whatever it is
		assertTrue(scn.IsHindered(squealer1));
		assertTrue(scn.IsHindered(squealer2));
		assertTrue(scn.IsHindered(khamul));
		assertTrue(scn.IsHindered(gimli));
		assertTrue(scn.IsHindered(sam));
		assertTrue(scn.IsHindered(frodo));
		assertTrue(scn.IsHindered(elrond));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignmentActionHindersCharactersAndNoOtherCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var fear = scn.GetShadowCard("fear");
		var fear2 = scn.GetShadowCard("fear2");
		var sky1 = scn.GetShadowCard("sky1");
		var steed = scn.GetShadowCard("steed");
		var squealer1 = scn.GetShadowCard("squealer1");
		var squealer2 = scn.GetShadowCard("squealer2");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");

		scn.MoveCardsToSupportArea(fear, fear2, sky1);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(gimli, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(squealer1, squealer2, witchking);
		scn.AttachCardsTo(witchking, steed);
		scn.AddBurdens(1);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		scn.ShadowUseCardAction(fear);
		scn.ShadowChooseCard(witchking);
		scn.ShadowChooseCard(aragorn);

		assertTrue(scn.IsHindered(fear));
		assertTrue(scn.IsHindered(squealer1));
		assertTrue(scn.IsHindered(squealer2));
		assertTrue(scn.IsHindered(gimli));
		assertTrue(scn.IsHindered(frodo));

		// The other copy is not the one that was used, and is no character.
		assertFalse(scn.IsHindered(fear2));
		// Nor is another condition, The One Ring, what the minion left standing bears, or what a hindered
		// companion bears.
		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(scn.GetRing()));
		assertFalse(scn.IsHindered(steed));
		assertFalse(scn.IsHindered(armor));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignmentActionIsNotOfferedWithTwoRingwraithMinionsAndANazgulOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var squealer1 = scn.GetShadowCard("squealer1");
		var squealer2 = scn.GetShadowCard("squealer2");
		var khamul = scn.GetShadowCard("khamul");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(fear);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(squealer1, squealer2, khamul, troop);
		scn.AddBurdens(1);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Four minions, two of them [ringwraith]; a burden to remove and an unbound companion are there.
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.IsHindered(fear));
		assertFalse(scn.ShadowActionAvailable(fear));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void AssignmentActionIsNotOfferedWithNoBurdenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var squealer1 = scn.GetShadowCard("squealer1");
		var squealer2 = scn.GetShadowCard("squealer2");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(fear);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(squealer1, squealer2, witchking);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Three [ringwraith] minions and an unbound companion, and no burden.
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.IsHindered(fear));
		assertFalse(scn.ShadowActionAvailable(fear));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void WithNoUnboundCompanionToSpotEveryCharacterButTheMinionIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var fear = scn.GetShadowCard("fear");
		var squealer1 = scn.GetShadowCard("squealer1");
		var squealer2 = scn.GetShadowCard("squealer2");
		var witchking = scn.GetShadowCard("witchking");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveCardsToSupportArea(fear);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(squealer1, squealer2, witchking);
		scn.AddBurdens(1);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Everything before "to" can be paid.
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(fear));

		scn.ShadowUseCardAction(fear);
		scn.ShadowChooseCard(witchking);

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.IsHindered(fear));
		assertFalse(scn.IsHindered(witchking));
		assertTrue(scn.IsHindered(squealer1));
		assertTrue(scn.IsHindered(squealer2));
		assertTrue(scn.IsHindered(sam));
		assertTrue(scn.IsHindered(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
