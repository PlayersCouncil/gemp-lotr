package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_176_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lemenya", "12_176");
					// Two [wraith] mounts for the discard pile, bearer must be a Nazgûl: Black Steed, cost 1 (strength +2 at a
					// plains, discarded at an underground site: site 3 is neither), and Winged Mount, cost 1 (to play, discard
					// 2 cards from hand; a response that removes a threat)
					put("steed", "1_208");
					put("winged", "8_83");
					// Fell Beast: a [wraith] mount, cost 0 (to play, discard 2 cards from hand), laid in the Free Peoples
					// player's own discard pile
					put("fell", "6_83");
					put("sword", "1_218");      // Nazgûl Sword: a [wraith] possession that is no mount; damage +1 only at 3 burdens
					// Threatening Warg: an [orc] mount, cost 2, bearer an [orc] Orc of strength 10 or less; its play trigger
					// wants 2 such Orcs, and there is one
					put("warg", "17_91");
					// Denizen of the Black Pit: an [orc] Orc of strength 10, who could bear the Warg; +2 only underground
					put("denizen", "11_117");
					// Úlairë Nelya, Ringwraith in Twilight: another Nazgûl, site 2; nothing below 2 burdens or 2 wounds on the
					// Ring-bearer
					put("nelya", "2_84");
					put("runner1", "1_178");    // Goblin Runner: two cards for the hand, which Winged Mount's play could discard
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireLemenyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Úlairë Lemenya, Black Enemy
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: When you play Úlairë Lemenya, you may play a [wraith] mount from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lemenya");

		assertEquals("Úlairë Lemenya", card.getBlueprint().getTitle());
		assertEquals("Black Enemy", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedSheMayPlayAWraithMountFromHerOwnersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");
		var winged = scn.GetShadowCard("winged");
		var sword = scn.GetShadowCard("sword");
		var warg = scn.GetShadowCard("warg");
		var denizen = scn.GetShadowCard("denizen");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var fell = scn.GetFreepsCard("fell");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetShadowHand(lemenya, runner1, runner2);
		scn.MoveCardsToDiscard(steed, winged, sword, warg, fell);
		assertInDiscard(steed, winged, sword, warg, fell);
		scn.MoveMinionsToTable(denizen);
		scn.FreepsPass();
		// Site 3 is her site number: she is not roaming.
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInHand(lemenya);
		assertInDiscard(steed, winged, sword, warg, fell);

		scn.ShadowPlayCard(lemenya);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lemenya));
		scn.ShadowAcceptOptionalTrigger();

		// Either [wraith] mount; not a [wraith] possession that is no mount, not an [orc] mount that the Denizen could
		// bear, not the Free Peoples player's own Fell Beast.  The "may" was the offer's: the mount itself is asked for.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(steed, winged));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, warg, fell));
		scn.ShadowChooseCard(steed);

		// She is the one Nazgûl, so the Steed goes on her unasked.
		assertAttachedTo(steed, lemenya);
		assertInDiscard(winged, sword, warg, fell);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesTheMountInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetShadowHand(lemenya);
		scn.MoveCardsToDiscard(steed);
		assertInDiscard(steed);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInDiscard(steed);

		scn.ShadowPlayCard(lemenya);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lemenya));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertInDiscard(steed);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NothingIsOfferedWithNoWraithMountInHerOwnersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var sword = scn.GetShadowCard("sword");
		var warg = scn.GetShadowCard("warg");
		var denizen = scn.GetShadowCard("denizen");
		var fell = scn.GetFreepsCard("fell");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetShadowHand(lemenya);
		scn.MoveCardsToDiscard(sword, warg, fell);
		assertInDiscard(sword, warg, fell);
		scn.MoveMinionsToTable(denizen);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInDiscard(sword, warg, fell);

		scn.ShadowPlayCard(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(lemenya));
		assertInDiscard(sword, warg, fell);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenThePoolLeftCannotPayForTheMount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetShadowHand(lemenya);
		scn.MoveCardsToDiscard(steed);
		assertInDiscard(steed);
		scn.FreepsPass();
		// 4 for her leaves 0, one short of the Steed's 1.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertInDiscard(steed);

		scn.ShadowPlayCard(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(lemenya));
		assertInDiscard(steed);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherNazgulPlayedAfterHerOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var steed = scn.GetShadowCard("steed");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetShadowHand(nelya);
		scn.MoveCardsToDiscard(steed);
		assertInDiscard(steed);
		scn.MoveMinionsToTable(lemenya);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInDiscard(steed);

		scn.ShadowPlayCard(nelya);
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(lemenya));
		assertInDiscard(steed);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
