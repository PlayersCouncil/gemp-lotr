package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_308_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rally", "7_308");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2; his text answers a weapon played on him
					put("pillager", "7_275");   // Gorgoroth Pillager: [sauron] Orc of another title; his text works only while he is stacked
					put("sentry", "10_84");     // Cirith Ungol Sentry: a [sauron] minion who is no Orc (Uruk-hai); damage +1 only
					put("hound", "7_192");      // Morgul Hound: an Orc who is not [sauron] ([wraith]); his text needs 2 wounds on the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RallytheHostStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Rally the Host
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 2 [sauron] Orcs.<br><b>Regroup:</b> Exert a [sauron] Orc and remove a threat to shuffle a [sauron] Orc from play into your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rally");

		assertEquals("Rally the Host", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilASecondSauronOrcIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rally = scn.GetShadowCard("rally");
		var chieftain = scn.GetShadowCard("chieftain");
		var pillager = scn.GetShadowCard("pillager");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(rally);
		assertInHand(rally);

		scn.StartGame();
		// One [sauron] Orc, beside a [sauron] minion who is no Orc and an Orc who is not [sauron].
		scn.MoveMinionsToTable(chieftain, sentry, hound);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(rally));

		// A second [sauron] Orc, of another title.
		scn.MoveMinionsToTable(pillager);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(rally));

		scn.ShadowPlayCard(rally);
		assertInZone(Zone.SUPPORT, rally);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsAnOrcAndRemovesAThreatToShuffleAnOrcIntoTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rally = scn.GetShadowCard("rally");
		var chieftain = scn.GetShadowCard("chieftain");
		var pillager = scn.GetShadowCard("pillager");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(rally);
		assertInZone(Zone.SUPPORT, rally);

		scn.StartGame();
		// No minion in the Shadow phase: straight on to the regroup phase.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(chieftain, pillager, sentry, hound);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.FreepsPass();

		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(chieftain));
		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(rally));

		scn.ShadowUseCardAction(rally);
		// The exertion: a [sauron] Orc of either title, not the Uruk-hai or the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, pillager));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(chieftain);

		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetThreats());
		// The Orc shuffled in: either [sauron] Orc, the one just exerted among them.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, pillager));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(pillager);

		assertInZone(Zone.DECK, pillager);
		assertEquals(deck + 1, scn.GetShadowDeckCount());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain, sentry, hound);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoThreatToRemoveTheRegroupAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rally = scn.GetShadowCard("rally");
		var chieftain = scn.GetShadowCard("chieftain");
		var pillager = scn.GetShadowCard("pillager");
		scn.MoveCardsToSupportArea(rally);
		assertInZone(Zone.SUPPORT, rally);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(chieftain, pillager);
		scn.FreepsPass();

		// Two [sauron] Orcs able to exert, and no threat.
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(pillager));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(rally));
	}

	@Test
	public void WithItsOnlySauronOrcExhaustedTheRegroupAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rally = scn.GetShadowCard("rally");
		var chieftain = scn.GetShadowCard("chieftain");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(rally);
		assertInZone(Zone.SUPPORT, rally);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(chieftain, sentry, hound);
		// Vitality 2 with 1 wound: exhausted.  The Uruk-hai and the [wraith] Orc could exert, but are no [sauron] Orcs.
		scn.AddWoundsToChar(chieftain, 1);
		scn.AddThreats(1);
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(1, scn.GetThreats());
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(rally));
	}
}
