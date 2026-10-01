package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_02_046_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("captain", "52_46");
					put("uruk", "1_151");    // Uruk Savage (non-unique Uruk-hai)
					put("lurtz", "1_127");   // Lurtz, Servant of Saruman (another unique Uruk-hai)
					put("brood", "1_154");   // Uruk Brood (Uruk-hai in discard to play)
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukCaptainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Uruk Captain
		 * Unique: true
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Shadow:</b> Remove (1) and exert a unique Uruk-hai
		 * to play an Uruk-hai from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("captain");

		assertEquals("Uruk Captain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void UrukCaptainExertsAUniqueUrukHaiToPlayFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var uruk = scn.GetShadowCard("uruk");
		var lurtz = scn.GetShadowCard("lurtz");
		var brood = scn.GetShadowCard("brood");

		scn.MoveMinionsToTable(captain, uruk, lurtz);
		scn.MoveCardsToDiscard(brood);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(captain));
		int twilight = scn.GetTwilight();
		scn.ShadowUseCardAction(captain);

		// Only unique Uruk-hai (Uruk Captain himself, Lurtz) may be exerted; the non-unique Uruk Savage may not
		assertTrue(scn.ShadowHasCardChoiceAvailable(captain, lurtz));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(uruk));
		scn.ShadowChooseCard(lurtz);

		assertEquals(1, scn.GetWoundsOn(lurtz));
		assertEquals(0, scn.GetWoundsOn(captain));
		assertEquals(0, scn.GetWoundsOn(uruk));
		// removed (1), then paid Uruk Brood's cost (2, +2 roaming at site 2)
		assertEquals(twilight - 1 - 2 - 2, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, brood);
	}

	@Test
	public void UrukCaptainCanExertHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var uruk = scn.GetShadowCard("uruk");
		var brood = scn.GetShadowCard("brood");

		scn.MoveMinionsToTable(captain, uruk);
		scn.MoveCardsToDiscard(brood);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowUseCardAction(captain);
		// the Captain is the only unique Uruk-hai, so he is exerted without a choice

		assertEquals(1, scn.GetWoundsOn(captain));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertInZone(Zone.SHADOW_CHARACTERS, brood);
	}

	@Test
	public void UrukCaptainAbilityNotAvailableWithOnlyNonUniqueUrukHaiAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var uruk = scn.GetShadowCard("uruk");
		var brood = scn.GetShadowCard("brood");

		scn.MoveMinionsToTable(captain, uruk);
		scn.MoveCardsToDiscard(brood);

		scn.StartGame();
		// the Captain (vitality 2) is exhausted; only the non-unique Uruk Savage could exert
		scn.AddWoundsToChar(captain, 1);
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowActionAvailable(captain));
	}

	@Test
	public void UrukCaptainAbilityRequiresRemovingOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var brood = scn.GetShadowCard("brood");

		scn.MoveMinionsToTable(captain);
		scn.MoveCardsToDiscard(brood);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.SetTwilight(0);
		assertFalse(scn.ShadowActionAvailable(captain));
	}
}
