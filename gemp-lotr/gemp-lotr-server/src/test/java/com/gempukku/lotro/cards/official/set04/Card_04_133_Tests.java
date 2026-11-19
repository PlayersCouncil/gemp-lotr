package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_133_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ruins", "4_133");
					// Faramir, Son of Denethor: Ring-bound [gondor] Man ranger, strength 7, vitality 3; his text only
					// stops the opponent's skirmish actions in his skirmishes
					put("faramir", "4_117");
					// Damrod, Ranger of Ithilien: Ring-bound [gondor] Man ranger; a skirmish ability used only on purpose
					put("damrod", "4_114");
					// Aragorn, Ranger of the North: [gondor] Man who is not Ring-bound; a maneuver ability used only on
					// purpose
					put("aragorn", "1_89");
					// Sam, Frodo's Gardener: a Ring-bound Hobbit; a fellowship ability used only on purpose
					put("sam", "4_315");
					// Dunlending Footmen: [dunland] Man, site 3, strength 11, vitality 2; its text is about its own cost
					put("footmen", "6_3");
					// Easterling Axeman: [raider] Man, site 4, strength 6, vitality 2; strength +3 and fierce only at 2
					// burdens
					put("axeman", "4_224");
					// Goblin Patrol Troop: [moria] Orc, site 4, strength 13, vitality 3, no text
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 7 of the Towers path is Helm's Tower 102_71 (its damage +1 wants 6 companions); site 6 is Caves of
	// Aglarond 4_352.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ruins", "4_133");
					// Faramir, Son of Denethor: Ring-bound [gondor] Man ranger
					put("faramir", "4_117");
					// Boromir, Steward's Heir: Ring-bound [gondor] Man knight and ranger; keywords only
					put("boromir", "0_65");
					// Aragorn, Ranger of the North: [gondor] Man who is not Ring-bound; a maneuver ability used only on
					// purpose
					put("aragorn", "1_89");
					// Sam, Frodo's Gardener: a Ring-bound Hobbit; a fellowship ability used only on purpose
					put("sam", "4_315");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RuinsofOsgiliathStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Ruins of Osgiliath
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 	While the fellowship is at site 7[T], each Ring-bound Man is <b>defender +1</b>.
		 * 	<b>Skirmish:</b> Spot a Ring-bound Man and discard this condition to wound an opponent's Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ruins");

		assertEquals("Ruins of Osgiliath", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachRingBoundManIsDefenderPlus1OnceTheFellowshipMovesToSite7OfTheTowersPath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var frodo = scn.GetRingBearer();
		var ruins = scn.GetFreepsCard("ruins");
		var faramir = scn.GetFreepsCard("faramir");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(faramir, boromir, aragorn, sam);
		scn.MoveCardsToSupportArea(ruins);
		assertInZone(Zone.SUPPORT, ruins);

		scn.StartGame();
		scn.SkipToSite(6);

		// At site 6 of the Towers path: nobody.
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetKeywordCount(faramir, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));

		// The fellowship moves to site 7.
		scn.FreepsPass();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetKeywordCount(faramir, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		// A Man who is not Ring-bound, and Ring-bound Hobbits.
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(sam, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7OfTheFellowshipPathNoRingBoundManIsDefender() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruins = scn.GetFreepsCard("ruins");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(faramir);
		scn.MoveCardsToSupportArea(ruins);
		assertInZone(Zone.SUPPORT, ruins);

		scn.StartGame();
		scn.SkipToSite(7);

		// Anduin Confluence 1_353, a site 7 of another block.
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, ruins);
		assertEquals(0, scn.GetKeywordCount(faramir, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsItselfToWoundAnOpponentsManInAnySkirmishAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruins = scn.GetFreepsCard("ruins");
		var faramir = scn.GetFreepsCard("faramir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var footmen = scn.GetShadowCard("footmen");
		var axeman = scn.GetShadowCard("axeman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(faramir, aragorn);
		scn.MoveCardsToSupportArea(ruins);
		assertInZone(Zone.SUPPORT, ruins);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(footmen, axeman, troop);
		// Vitality 2 with 1 wound: the Axeman is exhausted, and a wound kills him.
		scn.AddWoundsToChar(axeman, 1);
		assertEquals(1, scn.GetWoundsOn(axeman));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{faramir, troop},
				new PhysicalCardImpl[]{aragorn, footmen}
		);
		// The Axeman is left over, and the Shadow player puts him on Aragorn.
		scn.ShadowAssignToMinions(aragorn, axeman);
		scn.FreepsResolveSkirmish(faramir);

		assertTrue(scn.IsCharSkirmishingAgainst(faramir, troop));
		assertEquals(0, scn.GetWoundsOn(footmen));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(ruins));

		scn.FreepsUseCardAction(ruins);
		assertInZone(Zone.DISCARD, ruins);
		// The two Men are the Shadow player's, of two cultures, and in a skirmish still to come; the Orc in this
		// skirmish is no Man, and Faramir and Aragorn are Men of the player's own.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(footmen, axeman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, faramir, aragorn));
		scn.FreepsChooseCard(axeman);

		// One wound on an exhausted Man: killed.
		assertInZone(Zone.DISCARD, axeman);
		assertEquals(0, scn.GetWoundsOn(footmen));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithoutARingBoundManToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruins = scn.GetFreepsCard("ruins");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var footmen = scn.GetShadowCard("footmen");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToSupportArea(ruins);
		assertInZone(Zone.SUPPORT, ruins);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(footmen);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, footmen);

		// A Man who is not Ring-bound and a Ring-bound Hobbit; an opponent's Man to wound.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, footmen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ruins));
		assertInZone(Zone.SUPPORT, ruins);
	}

	@Test
	public void WithNoOpponentsManTheSkirmishAbilityIsOfferedAndDiscardsItForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruins = scn.GetFreepsCard("ruins");
		var damrod = scn.GetFreepsCard("damrod");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(damrod);
		scn.MoveCardsToSupportArea(ruins);
		assertInZone(Zone.SUPPORT, ruins);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(damrod, troop);

		// Damrod is the Ring-bound Man spotted; the only minion is an Orc.
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(damrod));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(ruins));

		scn.FreepsUseCardAction(ruins);
		assertInZone(Zone.DISCARD, ruins);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(damrod));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
