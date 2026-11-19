package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_125_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("exorciser", "18_125");
					put("exorciser2", "18_125");
					// Tracking Uruk: [uruk-hai] Uruk-hai, vitality 3; Hunter 1 is its whole text
					put("uruk", "15_171");
					// Saruman, Master of the White Hand: [uruk-hai] Wizard minion, vitality 4; each companion is
					// resistance -1, and a skirmish ability used only on purpose
					put("saruman", "17_116");
					// Cirith Ungol Guard: [sauron] Uruk-hai, vitality 2; Damage +1 only
					put("guard", "10_81");
					// White Hand Butcher: [uruk-hai] Uruk-hai, twilight 4; Hunter 6 is its whole text
					put("butcher", "17_122");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandExorciserStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: White Hand Exorciser
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 6
		 * Vitality: 1
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>When you play this minion, you may heal an [uruk-hai] minion.
		*/

		var scn = GetScenario();

		var exorciser = scn.GetFreepsCard("exorciser");

		assertEquals("White Hand Exorciser", exorciser.getBlueprint().getTitle());
		assertNull(exorciser.getBlueprint().getSubtitle());
		assertFalse(exorciser.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, exorciser.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, exorciser.getBlueprint().getCulture());
		assertEquals(CardType.MINION, exorciser.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, exorciser.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(exorciser, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(exorciser, Keyword.DAMAGE));
		assertEquals(2, exorciser.getBlueprint().getTwilightCost());
		assertEquals(6, exorciser.getBlueprint().getStrength());
		assertEquals(1, exorciser.getBlueprint().getVitality());
		assertEquals(5, exorciser.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedItMayHealAnUrukHaiMinionOfEitherRaceAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var exorciser = scn.GetShadowCard("exorciser");
		var exorciser2 = scn.GetShadowCard("exorciser2");
		var uruk = scn.GetShadowCard("uruk");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveCardsToHand(exorciser);
		assertInHand(exorciser);
		scn.FreepsPass(); // to site 2: the Shadow phase
		// A second copy already in play: only the copy played makes the offer.
		scn.MoveMinionsToTable(exorciser2, uruk, saruman, guard);
		scn.AddWoundsToChar(uruk, 2);
		scn.AddWoundsToChar(saruman, 1);
		scn.AddWoundsToChar(guard, 1);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(uruk));
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.ShadowPlayCard(exorciser);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(exorciser));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(exorciser2));
		scn.ShadowAcceptOptionalTrigger();

		// An [uruk-hai] Uruk-hai and an [uruk-hai] Wizard are offered; a [sauron] Uruk-hai is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(uruk, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard));
		// The offer was the "may"; once it is taken, one minion is to be healed.
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(uruk);

		// One wound of two is healed.
		assertEquals(1, scn.GetWoundsOn(uruk));
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoWoundedUrukHaiMinionNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var exorciser = scn.GetShadowCard("exorciser");
		var uruk = scn.GetShadowCard("uruk");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveCardsToHand(exorciser);
		assertInHand(exorciser);
		scn.FreepsPass(); // to site 2: the Shadow phase
		scn.MoveMinionsToTable(uruk, guard);
		scn.AddWoundsToChar(guard, 1);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(1, scn.GetWoundsOn(guard));

		// The only wounded minion is no [uruk-hai] minion, so the heal would do nothing.
		scn.ShadowPlayCard(exorciser);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherUrukHaiMinionPlayedAfterItDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var exorciser = scn.GetShadowCard("exorciser");
		var uruk = scn.GetShadowCard("uruk");
		var butcher = scn.GetShadowCard("butcher");

		scn.StartGame();
		scn.MoveCardsToHand(butcher);
		assertInHand(butcher);
		scn.FreepsPass(); // to site 2: the Shadow phase
		scn.MoveMinionsToTable(exorciser, uruk);
		scn.AddWoundsToChar(uruk, 2);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(uruk));

		scn.ShadowPlayCard(butcher);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(uruk));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferHealsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var exorciser = scn.GetShadowCard("exorciser");
		var uruk = scn.GetShadowCard("uruk");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveCardsToHand(exorciser);
		assertInHand(exorciser);
		scn.FreepsPass(); // to site 2: the Shadow phase
		scn.MoveMinionsToTable(uruk, saruman);
		scn.AddWoundsToChar(uruk, 2);
		scn.AddWoundsToChar(saruman, 1);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(uruk));
		assertEquals(1, scn.GetWoundsOn(saruman));

		scn.ShadowPlayCard(exorciser);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(exorciser));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(uruk));
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
