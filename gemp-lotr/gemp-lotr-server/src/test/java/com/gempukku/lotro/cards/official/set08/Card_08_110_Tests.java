package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_110_Tests
{

	// The King block, whose site 4 the trigger names: site 3 The Dimholt, site 4 Osgiliath Fallen (Shadow number 2),
	// site 5 Pelennor Flat (Shadow number 6; its trigger is at the start of the maneuver phase).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foothills", "8_110");
					put("sam", "1_311");         // Sam, Son of Hamfast: a Ring-bound [shire] Hobbit; abilities used only on purpose
					put("ranger", "4_122");      // Gondorian Ranger: a Ring-bound [gondor] Man; his text is about minions assigned to him
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a companion who is not Ring-bound
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship block: site 4 is Balin's Tomb, a site 4 of another block; site 5 The Bridge of Khazad-dum
	// (Shadow number 6).
	protected VirtualTableScenario GetFellowshipScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foothills", "8_110");
					put("ranger", "4_122");      // Gondorian Ranger: a Ring-bound [gondor] Man; his text is about minions assigned to him
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a companion who is not Ring-bound
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRingBoundRegroupScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foothills", "8_110");
					put("sam", "1_311");         // Sam, Son of Hamfast: a Ring-bound [shire] Hobbit; abilities used only on purpose
					put("ranger", "4_122");      // Gondorian Ranger: a Ring-bound [gondor] Man; his text is about minions assigned to him
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a companion who is not Ring-bound
					put("fearing", "2_100");     // Fearing the Worst: another [shire] support condition; it raises search cards and trackers only
					// Cards for the hand: a companion, a possession and a Shadow card
					put("gimli", "1_13");
					put("armor", "1_8");
					put("runner", "1_178");      // Goblin Runner: a minion the Free Peoples player cannot play; the Shadow player's copy is the maneuver phase's minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRegroupScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foothills", "8_110");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is not Ring-bound
					put("guard", "1_7");        // Dwarf Guard, Dwarven Armor, Gimli: three cards for the hand
					put("armor", "1_8");
					put("gimli", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgaiFoothillsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Morgai Foothills
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When the fellowship moves from site 4[K], remove (1) for each Ring-bound companion you spot.<br><b>Regroup:</b> Discard a card from hand for each Ring-bound companion you spot. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("foothills");

		assertEquals("Morgai Foothills", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// F-0418 (Ketura, 2026-10-11: "yeah clearly a bug"): "Regroup: Discard a card from hand for each Ring-bound
	// companion you spot.  Discard this condition."  The ability discarded the condition and no card from hand.
	@Test
	public void TheRegroupAbilityDiscardsACardFromHandForTheOneRingBoundCompanionAndThenTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var foothills = scn.GetFreepsCard("foothills");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(foothills);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(guard, armor, gimli);
		assertInHand(guard, armor, gimli);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// Frodo is the one Ring-bound companion; Aragorn is not counted.  Three cards in hand.
		assertEquals(3, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, foothills);
		assertTrue(scn.FreepsActionAvailable(foothills));
		scn.FreepsUseCardAction(foothills);

		// One card for the one Ring-bound companion, of the player's choosing among the three.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, armor, gimli));
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(armor);

		assertInDiscard(armor);
		assertInHand(guard, gimli);
		assertEquals(2, scn.GetFreepsHandCount());
		assertInDiscard(foothills);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void MovingFromSite4OfTheKingPathRemovesOneForEachRingBoundCompanionSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foothills = scn.GetFreepsCard("foothills");
		var ranger = scn.GetFreepsCard("ranger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(ranger, aragorn);
		scn.MoveCardsToSupportArea(foothills);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetTwilight(5);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, foothills);

		scn.FreepsPass();

		// Frodo ([shire] Hobbit) and the Gondorian Ranger ([gondor] Man) are Ring-bound; Aragorn is not.  How many to
		// spot is the player's, from 0 and filled in with all of them; nothing is removed yet.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertEquals(2, (int) scn.FreepsGetChoiceDefault());
		assertEquals(5, scn.GetTwilight());
		scn.FreepsChoose("2");

		// 5 - 2 removed, then the move: + 6 for Pelennor Flat + 3 companions
		assertEquals(12, scn.GetTwilight());
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreeRingBoundCompanionsThePlayerMaySpotFewer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foothills = scn.GetFreepsCard("foothills");
		var sam = scn.GetFreepsCard("sam");
		var ranger = scn.GetFreepsCard("ranger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, ranger, aragorn);
		scn.MoveCardsToSupportArea(foothills);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, foothills);

		scn.FreepsPass();

		// Frodo, Sam and the Ranger.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		assertEquals(3, (int) scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("1");

		// 5 - 1 removed, then the move: + 6 for Pelennor Flat + 4 companions
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheMoveToSite4OfTheKingPathRemovesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foothills = scn.GetFreepsCard("foothills");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToSupportArea(foothills);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetTwilight(5);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, foothills);

		scn.FreepsPass();

		// Nothing asked: 5, + 2 for Osgiliath Fallen + 2 companions
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingFromSite4OfTheFellowshipPathRemovesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var foothills = scn.GetFreepsCard("foothills");
		var ranger = scn.GetFreepsCard("ranger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(ranger, aragorn);
		scn.MoveCardsToSupportArea(foothills);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetTwilight(5);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, foothills);

		scn.FreepsPass();

		// Balin's Tomb is a site 4 of another block.  Nothing asked: 5, + 6 for The Bridge of Khazad-dum + 3
		// companions
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityWithThreeRingBoundCompanionsDiscardsUpToThreeCardsOfThePlayersChoosing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBoundRegroupScenario();

		var foothills = scn.GetFreepsCard("foothills");
		var sam = scn.GetFreepsCard("sam");
		var ranger = scn.GetFreepsCard("ranger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var fearing = scn.GetFreepsCard("fearing");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(sam, ranger, aragorn);
		scn.MoveCardsToSupportArea(foothills, fearing);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetFreepsHand(gimli, armor, runner);
		assertInHand(gimli, armor, runner);
		assertInZone(Zone.SUPPORT, foothills, fearing);

		assertTrue(scn.FreepsActionAvailable(foothills));
		scn.FreepsUseCardAction(foothills);

		// Frodo, Sam and the Ranger: from none to three, any card of the hand, a Shadow card among them.  Two chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, armor, runner));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(armor, runner);

		assertInDiscard(armor, runner);
		assertInHand(gimli);
		assertEquals(1, scn.GetFreepsHandCount());
		// This condition is discarded, and not the other [shire] condition.
		assertInDiscard(foothills);
		assertInZone(Zone.SUPPORT, fearing);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBoundRegroupScenario();

		var foothills = scn.GetFreepsCard("foothills");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(foothills);

		scn.StartGame();
		scn.MoveCardsToHand(gimli);
		scn.MoveMinionsToTable(runner);
		assertInHand(gimli);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.SUPPORT, foothills);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(foothills));
	}
}
