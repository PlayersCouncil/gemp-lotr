package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_054_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scimitar", "5_54");
					put("scimitar2", "5_54");
					put("rider", "5_67");       // Warg-rider: [isengard] Orc, a warg-rider; fierce while not exhausted
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, no warg-rider; a Regroup ability only
					put("party", "1_158");      // Uruk-hai Raiding Party: an [isengard] Uruk-hai, damage +1 and no other text
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text
					put("banner", "6_59");      // Banner of Isengard: an [isengard] hand weapon of cost 0 on an [isengard] minion; a win trigger only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardScimitarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Isengard Scimitar
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be an [isengard] Orc.
		 * When you play this weapon, you may add (1) for each site you control.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scimitar");

		assertEquals("Isengard Scimitar", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void GoesOnlyOnAnIsengardOrcAndItsAddMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scimitar = scn.GetShadowCard("scimitar");
		var rider = scn.GetShadowCard("rider");
		var worker = scn.GetShadowCard("worker");
		var party = scn.GetShadowCard("party");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(scimitar);
		assertInHand(scimitar);

		scn.StartGame();
		// Player 1's third turn: the Shadow player holds site 1; the fellowship moves on to site 4.
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.MoveMinionsToTable(rider, worker, party, troop);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(scimitar);

		// Each [isengard] Orc, a warg-rider or not; not the [isengard] Uruk-hai, nor the [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(rider, worker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(party, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(worker);
		assertAttachedTo(scimitar, worker);

		// Declined: the (1) for his site is not added.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scimitar));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoSiteOfItsPlayersOwnItWouldAddNothingAndIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scimitar = scn.GetShadowCard("scimitar");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToHand(scimitar);
		assertInHand(scimitar);

		scn.StartGame();
		// Player 1's second turn: site 1 is behind both markers, and the Free Peoples player holds it.
		scn.SkipToSite(2);
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		assertTrue(scn.FreepsControls(site1));
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		scn.MoveMinionsToTable(worker);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(scimitar);
		assertAttachedTo(scimitar, worker);

		// The Shadow player controls no site: a yes would add nothing (Q-0033, Q-0034).
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scimitar));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(pool, scn.GetTwilight());
	}

	@Test
	public void WhenPlayedItAddsOneForEachSiteItsPlayerControlsAndNotForTheOtherPlayersSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scimitar = scn.GetShadowCard("scimitar");
		var banner = scn.GetShadowCard("banner");
		var worker = scn.GetShadowCard("worker");
		var party = scn.GetShadowCard("party");
		scn.MoveCardsToHand(scimitar, banner);
		assertInHand(scimitar, banner);

		scn.StartGame();
		// Site 1 to the Free Peoples player, sites 2 and 3 to the Shadow player; the fellowship moves on to site 5.
		scn.SkipToSite(4);
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertEquals(2, scn.GetShadowControlledSiteCount());
		scn.MoveMinionsToTable(worker, party);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		// The Worker is its one bearer here.
		scn.ShadowPlayCard(scimitar);
		assertAttachedTo(scimitar, worker);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scimitar));
		scn.ShadowAcceptOptionalTrigger();

		// Two sites of his own: (2), not (3).
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [isengard] hand weapon played after it: nothing is offered.
		// The Worker bears a hand weapon already: the Raiding Party, an [isengard] minion, is the one bearer left.
		scn.ShadowPlayCard(banner);
		assertAttachedTo(banner, party);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scimitar));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(pool + 2, scn.GetTwilight());
	}

	@Test
	public void WithOneSiteItAddsOneAndASecondCopyOffersOnlyForItsOwnPlayWhichMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scimitar = scn.GetShadowCard("scimitar");
		var scimitar2 = scn.GetShadowCard("scimitar2");
		var rider = scn.GetShadowCard("rider");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToHand(scimitar, scimitar2);
		assertInHand(scimitar, scimitar2);

		scn.StartGame();
		// Site 1 to the Free Peoples player, site 2 to the Shadow player; the fellowship moves on to site 4.
		scn.SkipToSite(3);
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertEquals(1, scn.GetShadowControlledSiteCount());
		scn.MoveMinionsToTable(rider, worker);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(scimitar);
		scn.ShadowChooseCard(worker);
		assertAttachedTo(scimitar, worker);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scimitar));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(pool + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The Worker bears a hand weapon already: the Warg-rider is the one bearer left.
		scn.ShadowPlayCard(scimitar2);
		assertAttachedTo(scimitar2, rider);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scimitar2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scimitar));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(pool + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
