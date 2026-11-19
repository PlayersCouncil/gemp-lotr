package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_117_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grunt", "13_117");
					put("grunt2", "13_117");
					put("goblin", "11_137");     // Scurrying Goblin: [orc] Orc, cost 1; its trigger needs an underground site
					put("halftroll", "14_12");   // Half-troll of Far Harad: [orc] Half-troll; its start-of-assignment trigger reaches no test here
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc minion, no text
					put("beckoning", "12_83");   // The Beckoning Shadow: [orc] support-area condition; nothing below 5 burdens
					put("beckoning2", "12_83");
					put("blow", "15_115");       // Pummeling Blow: [orc] condition borne by the fellowship's current site
					put("grond", "18_82");       // Grond, Forged With Black Steel: [orc] support-area possession; a regroup-move trigger only
					put("sbof", "1_278");        // Strength Born of Fear: [sauron] support-area condition, a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrdnanceGruntStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Ordnance Grunt
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>. <b>Fierce</b>. To play, spot an [orc] minion.<br><b>Shadow:</b> Discard an [orc] condition from play to play this minion from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grunt");

		assertEquals("Ordnance Grunt", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.TROLL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedFromHandUntilAnOrcMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grunt = scn.GetShadowCard("grunt");
		var goblin = scn.GetShadowCard("goblin");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToSupportArea(beckoning);
		scn.MoveCardsToHand(grunt, goblin);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, beckoning);
		assertInHand(grunt, goblin);
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(12);
		scn.FreepsPass();

		// An Orc minion of another culture, and an [orc] card that is no minion.
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(grunt));

		scn.ShadowPlayCard(goblin);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);

		assertTrue(scn.ShadowPlayAvailable(grunt));
		scn.ShadowPlayCard(grunt);
		assertInZone(Zone.SHADOW_CHARACTERS, grunt);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityDiscardsAnOrcConditionFromPlayToPlayThisCopyFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grunt = scn.GetShadowCard("grunt");
		var grunt2 = scn.GetShadowCard("grunt2");
		var halftroll = scn.GetShadowCard("halftroll");
		var beckoning = scn.GetShadowCard("beckoning");
		var beckoning2 = scn.GetShadowCard("beckoning2");
		var blow = scn.GetShadowCard("blow");
		var grond = scn.GetShadowCard("grond");
		var sbof = scn.GetShadowCard("sbof");
		scn.MoveCardsToSupportArea(beckoning, grond, sbof);
		scn.MoveCardsToDiscard(grunt, grunt2);
		scn.MoveCardsToHand(beckoning2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, beckoning, grond, sbof);
		assertInZone(Zone.DISCARD, grunt, grunt2);
		assertInHand(beckoning2);
		scn.MoveMinionsToTable(halftroll);
		scn.FreepsPass();
		// Shadow phase at Ettenmoors: 6 to play + 2 roaming.
		scn.SetTwilight(10);
		scn.AttachCardsTo(scn.GetCurrentSite(), blow);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.ATTACHED, blow);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(grunt));
		scn.ShadowUseCardAction(grunt);

		assertEquals(1, scn.ShadowGetChoiceMin());
		// [orc] conditions in the support area and on a site.
		assertTrue(scn.ShadowHasCardChoiceAvailable(beckoning, blow));
		// An [orc] possession, a [sauron] condition, and an [orc] condition in hand, not in play.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(grond, sbof, beckoning2));
		scn.ShadowChooseCard(blow);

		assertInZone(Zone.DISCARD, blow);
		assertInZone(Zone.SUPPORT, beckoning, grond, sbof);
		assertInZone(Zone.SHADOW_CHARACTERS, grunt);
		assertInZone(Zone.DISCARD, grunt2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoOrcConditionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grunt = scn.GetShadowCard("grunt");
		var halftroll = scn.GetShadowCard("halftroll");
		var beckoning = scn.GetShadowCard("beckoning");
		var grond = scn.GetShadowCard("grond");
		var sbof = scn.GetShadowCard("sbof");
		scn.MoveCardsToSupportArea(grond, sbof);
		scn.MoveCardsToDiscard(grunt);
		scn.MoveCardsToHand(beckoning);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, grond, sbof);
		assertInZone(Zone.DISCARD, grunt);
		assertInHand(beckoning);
		scn.MoveMinionsToTable(halftroll);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(grunt));
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilThePoolCanPayForThePlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grunt = scn.GetShadowCard("grunt");
		var halftroll = scn.GetShadowCard("halftroll");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToSupportArea(beckoning);
		scn.MoveCardsToDiscard(grunt);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, beckoning);
		assertInZone(Zone.DISCARD, grunt);
		scn.MoveMinionsToTable(halftroll);
		scn.FreepsPass();

		// One short of 6 to play + 2 roaming at Ettenmoors.
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(grunt));

		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(grunt));
		scn.ShadowUseCardAction(grunt);
		// The only [orc] condition is discarded unasked.
		assertInZone(Zone.DISCARD, beckoning);
		assertInZone(Zone.SHADOW_CHARACTERS, grunt);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoOrcMinionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grunt = scn.GetShadowCard("grunt");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToSupportArea(beckoning);
		scn.MoveCardsToDiscard(grunt);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, beckoning);
		assertInZone(Zone.DISCARD, grunt);
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The play from the discard pile is a play like any other: "To play, spot an [orc] minion" holds there too.
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(grunt));
	}
}
