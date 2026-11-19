package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_044_Tests
{
	// The Hobbit table: the Hobbit path with Beorn's House as site 4 in place of Goblin Town 30_52, Bilbo, Reliable
	// Companion 30_46 as the Ring-bearer and no card in the Ring slot. Mirkwood 30_53, site 5, discards Gandalf as
	// the fellowship moves there, which is the discard the House prevents.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "30_27");    // Gandalf, The Grey: [gandalf] Wizard companion; nothing that stops a discard, and his texts not used
					put("radagast", "31_17");   // Radagast, The Brown: [gandalf] Wizard ally; a Fellowship ability, not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion; a Fellowship ability, not used
					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] companion; a Maneuver ability, not used

					// [gandalf] staffs: an artifact and a possession, each "Bearer must be a Wizard".
					put("staff", "31_18");      // Wizard Staff: [gandalf] artifact, unique; a start-of-turn offer (laid after it)
					put("wizardstaff", "2_29"); // Wizard Staff: [gandalf] possession; a Skirmish ability, not used
					put("wizardstaff2", "2_29");
					// Cards that are not [gandalf] staffs.
					put("glamdring", "30_29");  // Glamdring: [gandalf] artifact, hand weapon, on Gandalf; a trigger on his win
					put("saruman", "32_24");    // Saruman, The White: [gandalf] Wizard ally; his abilities not used
					put("sarumanstaff", "4_174"); // Saruman's Staff: [isengard] staff, the Shadow player's, borne by Saruman

					// Fierce in Despair: a regroup Shadow event that discards a companion (except the Ring-bearer), with two
					// [raider] Men spotted. No Hobbit-set Shadow card discards a companion.
					put("despair", "7_148");
					put("despair2", "7_148");
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, strength 6; +3 and fierce at 2 burdens only
					put("axeman2", "4_224");
					put("aggressor", "30_39");  // Orkish Aggressor: a minion, so that there is a maneuver phase
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.HobbitSites);
					put("site4", "31_44");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BeornsHouseStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Beorn's House
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 4
		 * Shadow Number: 4
		 * Direction: none
		 * Game Text: <b>Fellowship:</b> Spot a [gandalf] staff or play it from your draw deck to prevent Gandalf from
		 * 	being discarded until the end of turn.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Beorn's House", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
	}

	@Test
	public void SpottingAStaffOnRadagastKeepsGandalfInPlayAsTheFellowshipMovesToMirkwood() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var staff = scn.GetFreepsCard("staff");
		var wizardstaff = scn.GetFreepsCard("wizardstaff");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(radagast);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.AttachCardsTo(radagast, staff);
		scn.MoveCardsToBottomOfDeck(wizardstaff);
		scn.FreepsRefreshDecision();
		var house = scn.GetCurrentSite();
		assertEquals("Beorn's House", house.getBlueprint().getTitle());
		assertAttachedTo(staff, radagast);
		assertInZone(Zone.DECK, wizardstaff);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		assertTrue(scn.FreepsActionAvailable(house));
		scn.FreepsUseCardAction(house);
		// A staff in play and one in the draw deck: the player is asked which way.
		assertEquals(List.of("Spot a GANDALF staff", "Play a GANDALF staff from your draw deck"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Spot a GANDALF staff");
		// A spot pays nothing: the Staff stays where it is, and the deck is not searched.
		assertAttachedTo(staff, radagast);
		assertInZone(Zone.DECK, wizardstaff);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals("Mirkwood", scn.GetCurrentSite().getBlueprint().getTitle());
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingAStaffFromTheDrawDeckKeepsGandalfInPlayAsTheFellowshipMovesToMirkwood() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var staff = scn.GetFreepsCard("staff");
		var wizardstaff = scn.GetFreepsCard("wizardstaff");
		var glamdring = scn.GetFreepsCard("glamdring");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(radagast);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToBottomOfDeck(staff, wizardstaff, glamdring);
		scn.FreepsRefreshDecision();
		var house = scn.GetCurrentSite();
		assertEquals("Beorn's House", house.getBlueprint().getTitle());
		assertInZone(Zone.DECK, staff, wizardstaff, glamdring);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		// No staff in play: the only way is to play one from the draw deck, and nothing is asked.
		assertTrue(scn.FreepsActionAvailable(house));
		scn.FreepsUseCardAction(house);
		scn.FreepsDismissRevealedCards();
		// Both [gandalf] staffs, the artifact and the possession; not Glamdring, a [gandalf] hand weapon.
		assertTrue(scn.FreepsHasCardChoiceAvailable(staff, wizardstaff));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(glamdring));
		scn.FreepsChooseCardBPFromSelection(staff);
		// Bearer must be a Wizard: Gandalf or Radagast.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, radagast));
		scn.FreepsChooseCard(gandalf);
		assertAttachedTo(staff, gandalf);
		assertInZone(Zone.DECK, wizardstaff, glamdring);
		// The deck searched is shuffled (CR 4.2 "draw deck"): a card played out of it makes two shuffles.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());

		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AStaffOfAnotherCultureOrAGandalfCardThatIsNoStaffIsNotSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var saruman = scn.GetFreepsCard("saruman");
		var glamdring = scn.GetFreepsCard("glamdring");
		var sarumanstaff = scn.GetShadowCard("sarumanstaff");
		var staff = scn.GetFreepsCard("staff");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(saruman);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.AttachCardsTo(saruman, sarumanstaff);
		scn.MoveCardsToBottomOfDeck(staff);
		scn.FreepsRefreshDecision();
		var house = scn.GetCurrentSite();
		assertAttachedTo(glamdring, gandalf);
		assertAttachedTo(sarumanstaff, saruman);
		assertInZone(Zone.DECK, staff);

		// Nothing spotted: the use goes straight to the play from the draw deck, with no choice of ways.
		scn.FreepsUseCardAction(house);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(staff));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(glamdring, sarumanstaff));
		scn.FreepsChooseCardBPFromSelection(staff);
		// Saruman bears a staff already: Gandalf is the one Wizard who may bear it.
		assertAttachedTo(staff, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoGandalfStaffInPlayOrInTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var staff = scn.GetFreepsCard("staff");
		var wizardstaff = scn.GetFreepsCard("wizardstaff");
		var wizardstaff2 = scn.GetFreepsCard("wizardstaff2");
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToDiscard(staff, wizardstaff, wizardstaff2);
		scn.FreepsRefreshDecision();
		var house = scn.GetCurrentSite();
		assertAttachedTo(glamdring, gandalf);
		assertInDiscard(staff, wizardstaff, wizardstaff2);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(house));
	}

	@Test
	public void WithNoGandalfInPlayAStaffIsStillPlayedFromTheDrawDeckForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		var staff = scn.GetFreepsCard("staff");
		scn.MoveAlliesToTable(radagast);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToBottomOfDeck(staff);
		scn.FreepsRefreshDecision();
		var house = scn.GetCurrentSite();
		assertInZone(Zone.DECK, staff);
		assertInZone(Zone.SUPPORT, radagast);

		// No Gandalf to protect: the ability is still offered, and its cost is paid.
		assertTrue(scn.FreepsActionAvailable(house));
		scn.FreepsUseCardAction(house);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(staff);
		assertAttachedTo(staff, radagast);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AttachCardsTo(gandalf, staff);
		assertAttachedTo(staff, gandalf);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(scn.GetCurrentSite()));
	}

	@Test
	public void GandalfCannotBeDiscardedUntilTheEndOfTheTurnAndAnotherCompanionCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var radagast = scn.GetFreepsCard("radagast");
		var wizardstaff = scn.GetFreepsCard("wizardstaff");
		var wizardstaff2 = scn.GetFreepsCard("wizardstaff2");
		var despair = scn.GetShadowCard("despair");
		var despair2 = scn.GetShadowCard("despair2");
		var axeman = scn.GetShadowCard("axeman");
		var axeman2 = scn.GetShadowCard("axeman2");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, balin, nori);
		scn.MoveAlliesToTable(radagast);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.AttachCardsTo(radagast, wizardstaff);
		// A second copy in the draw deck, which Gandalf, a Wizard with no staff, could bear: both ways can be taken.
		scn.MoveCardsToBottomOfDeck(wizardstaff2);
		scn.FreepsRefreshDecision();
		assertAttachedTo(wizardstaff, radagast);
		assertInZone(Zone.DECK, wizardstaff2);
		// The possession Wizard Staff, borne by Radagast, is spotted.
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChoose("Spot a GANDALF staff");
		assertAttachedTo(wizardstaff, radagast);
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertInZone(Zone.FREE_CHARACTERS, gandalf);

		// The regroup phase of the same turn: Fierce in Despair offers Balin and Nori, and not Gandalf.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(axeman, axeman2);
		scn.MoveCardsToHand(despair);
		scn.SetTwilight(20);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(despair));
		scn.ShadowPlayCard(despair);
		assertTrue(scn.ShadowHasCardChoiceAvailable(balin, nori));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gandalf, bilbo));
		scn.ShadowChooseCard(balin);
		assertInDiscard(balin);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);

		// The player's next turn: Gandalf may be discarded again.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		scn.SkipToPhase(Phase.REGROUP);
		// Esgaroth (site 6) makes no start-of-regroup offer: no [esgaroth] or [dwarven] ally in the deck (F-0804).
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(axeman, axeman2);
		scn.MoveCardsToHand(despair2);
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.ShadowPlayCard(despair2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gandalf, nori));
		scn.ShadowChooseCard(gandalf);
		assertInDiscard(gandalf);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
