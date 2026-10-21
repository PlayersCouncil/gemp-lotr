package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_10_073_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("deeds", "10_73");

					// The [rohan] Men, and the companions who nearly are.
					put("rider", "4_286");      // Rider of Rohan: a [rohan] Man companion; his skirmish ability needs him mounted
					// Hlafwine, Village Farmhand: a [rohan] Man ally, cost 1, home 4T (not on this path); his skirmish ability is used only on purpose
					put("hlafwine", "4_281");
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit, and a Man only while 3 [rohan] companions (or Éowyn) can be spotted
					put("merry", "103_85");
					// Aragorn, Ranger of the North: a Man companion of another culture ([gondor]); a maneuver ability used only on purpose
					put("aragorn", "1_89");

					// Elendil's Valor: Maneuver event, cost 0: "Make a [gondor] companion defender +1 until the regroup phase."
					// Aragorn is the only [gondor] companion on each board where it is played, and is chosen unasked.
					// Each play takes a card from the hand.
					put("valor", "1_103");
					put("valor2", "1_103");
					put("valor3", "1_103");
					// Dwarven Armor: cards to hold in hand and nothing else (no Dwarf to bear one).
					put("filler1", "1_8");
					put("filler2", "1_8");
					put("filler3", "1_8");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FellDeedsAwakeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Fell Deeds Awake
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a [rohan] Man.<br>Each time you lose initiative, you may liberate a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("deeds");

		assertEquals("Fell Deeds Awake", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilARohanManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deeds = scn.GetFreepsCard("deeds");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		// A [rohan] Hobbit and a [gondor] Man.
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveCardsToHand(deeds, hlafwine);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(deeds));

		// A [rohan] Man ally makes one.
		scn.FreepsPlayCard(hlafwine);
		assertInZone(Zone.SUPPORT, hlafwine);
		assertTrue(scn.FreepsPlayAvailable(deeds));
		scn.FreepsPlayCard(deeds);
		assertInZone(Zone.SUPPORT, deeds);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ItsOwnPlayBesideARohanManCompanionLosesInitiativeAndLiberatesTheHighestSiteAnOpponentControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deeds = scn.GetFreepsCard("deeds");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(rider);

		scn.StartGame();
		scn.SkipToSite(4);
		// Site 1 to the Free Peoples player, sites 2 and 3 to the Shadow player.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.ShadowControls(site3));

		scn.SetFreepsHand(deeds, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Four cards to three: the loss of initiative is answered with the card in play.
		assertTrue(scn.FreepsPlayAvailable(deeds));
		scn.FreepsPlayCard(deeds);
		assertInZone(Zone.SUPPORT, deeds);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(deeds));
		scn.FreepsAcceptOptionalTrigger();

		// The highest site an opponent controls goes back on the path; his lower one and the Free Peoples
		// player's own stay where they are.
		assertInZone(Zone.ADVENTURE_PATH, site3);
		assertFalse(scn.ShadowControls(site3));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachLossOfInitiativeOffersToLiberateASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deeds = scn.GetFreepsCard("deeds");
		var aragorn = scn.GetFreepsCard("aragorn");
		var valor = scn.GetFreepsCard("valor");
		var valor2 = scn.GetFreepsCard("valor2");
		var valor3 = scn.GetFreepsCard("valor3");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(deeds);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetFreepsHand(valor, valor2, valor3, scn.GetFreepsCard("filler1"));

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.ShadowControls(site3));

		// Four cards to three: initiative is lost, and the offer is made. Declined, nothing changes.
		scn.FreepsPlayCard(valor);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(deeds));
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.ShadowControls(site3));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A card more makes four: initiative is back from the next answer on.
		scn.MoveCardsToHand(scn.GetFreepsCard("filler2"));
		scn.ShadowPass();
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		// Lost a second time: offered again, and taken, site 3 is liberated.
		scn.FreepsPlayCard(valor2);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(deeds));
		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.ADVENTURE_PATH, site3);
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.MoveCardsToHand(scn.GetFreepsCard("filler3"));
		scn.ShadowPass();
		assertTrue(scn.FreepsHasInitiative());

		// A third time: site 2 is the highest the Shadow player controls now. The Free Peoples player's own site
		// 1 is never liberated.
		scn.FreepsPlayCard(valor3);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(deeds));
		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertFalse(scn.ShadowControls(site2));
		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AHandGoingFromThreeToTwoOrFromFiveToFourOrBackToFourOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deeds = scn.GetFreepsCard("deeds");
		var aragorn = scn.GetFreepsCard("aragorn");
		var valor = scn.GetFreepsCard("valor");
		var valor2 = scn.GetFreepsCard("valor2");
		var valor3 = scn.GetFreepsCard("valor3");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(deeds);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		// The hand is made three before any site is controlled: the loss of initiative that cheat makes is
		// answered in the fellowship phase, with nothing to liberate.
		scn.SetFreepsHand(valor, valor2, valor3);
		// Site 1 to the Shadow player: there is a site to liberate from then on.
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Three cards to two: the Shadow has initiative already, so none is lost.
		assertTrue(scn.ShadowControls(site1));
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		scn.FreepsPlayCard(valor);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(deeds));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Three cards more make five: the Shadow loses initiative at the next answer, which is no loss of the
		// Free Peoples player's.
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));
		scn.ShadowPass();
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(deeds));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Five cards to four: initiative is kept.
		scn.FreepsPlayCard(valor2);
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(deeds));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();
		assertTrue(scn.ShadowControls(site1));

		// Four cards to three on the same board: the offer is made.
		scn.FreepsPlayCard(valor3);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(deeds));
	}

	@Test
	public void WithOnlyItsOwnPlayersSiteControlledALossOfInitiativeOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deeds = scn.GetFreepsCard("deeds");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(rider);

		scn.StartGame();
		scn.SkipToSite(2);
		// A site is controlled, but it is the Free Peoples player's own, which he cannot liberate.
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		assertTrue(scn.FreepsControls(site1));

		scn.SetFreepsHand(deeds, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		scn.FreepsPlayCard(deeds);
		assertInZone(Zone.SUPPORT, deeds);
		assertFalse(scn.FreepsHasInitiative());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(deeds));
		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
