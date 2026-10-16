package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_149_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("messenger", "1_149");
					put("messenger2", "1_149"); // a second copy: not unique, and each has the text for itself
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					// Saruman's Chill: [isengard] weather condition played on a site by exerting an [isengard] minion;
					// its other text is at a move and at the end of the turn
					put("chill", "1_134");
					// Urgency: [isengard] weather condition in the support area; its text answers a move (no Ent is on
					// the table) and a skirmish ability used only on purpose
					put("urgency", "19_17");
					// No Ordinary Storm: [isengard] condition in the support area that is no weather; its text answers
					// the play of a weather condition
					put("storm", "1_130");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability used only on purpose
					put("cloak", "1_98");       // Boromir's Cloak: "Maneuver: Exert Boromir to discard a weather condition"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukMessengerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk Messenger
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 6
		 * Vitality: 1
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While you can spot a weather condition, this minion is strength +3.
		*/

		var scn = GetScenario();

		var messenger = scn.GetFreepsCard("messenger");

		assertEquals("Uruk Messenger", messenger.getBlueprint().getTitle());
		assertNull(messenger.getBlueprint().getSubtitle());
		assertFalse(messenger.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, messenger.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, messenger.getBlueprint().getCulture());
		assertEquals(CardType.MINION, messenger.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, messenger.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(messenger, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(messenger, Keyword.DAMAGE));
		assertEquals(2, messenger.getBlueprint().getTwilightCost());
		assertEquals(6, messenger.getBlueprint().getStrength());
		assertEquals(1, messenger.getBlueprint().getVitality());
		assertEquals(5, messenger.getBlueprint().getSiteNumber());
	}

	@Test
	public void StrengthPlusThreeWhileAWeatherConditionIsPlayedAndNoLongerOnceItIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, cloak);

		var messenger = scn.GetShadowCard("messenger");
		var savage = scn.GetShadowCard("savage");
		var chill = scn.GetShadowCard("chill");
		scn.MoveCardsToHand(chill);

		scn.StartGame();
		scn.MoveMinionsToTable(messenger, savage);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(6, scn.GetStrength(messenger));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(0, scn.GetWoundsOn(savage));

		// The Messenger, of vitality 1, cannot exert: the Savage pays for the Chill.
		scn.ShadowPlayCard(chill);
		scn.ShadowChooseCard(scn.GetCurrentSite());
		assertEquals(1, scn.GetWoundsOn(savage));
		assertAttachedTo(chill, scn.GetCurrentSite());
		assertEquals(9, scn.GetStrength(messenger));
		// The other minion is not.
		assertEquals(5, scn.GetStrength(savage));

		// Boromir's Cloak discards the Chill in the maneuver phase, and the bonus is gone.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(9, scn.GetStrength(messenger));
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.FreepsUseCardAction(cloak);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertInZone(Zone.DISCARD, chill);
		assertEquals(6, scn.GetStrength(messenger));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AWeatherConditionInTheSupportAreaCountsAndAConditionThatIsNoWeatherDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var messenger = scn.GetShadowCard("messenger");
		var messenger2 = scn.GetShadowCard("messenger2");
		var savage = scn.GetShadowCard("savage");
		var storm = scn.GetShadowCard("storm");
		var urgency = scn.GetShadowCard("urgency");

		scn.StartGame();
		scn.MoveMinionsToTable(messenger, messenger2, savage);
		assertEquals(6, scn.GetStrength(messenger));
		assertEquals(6, scn.GetStrength(messenger2));

		// An [isengard] condition that is no weather.
		scn.MoveCardsToSupportArea(storm);
		assertInZone(Zone.SUPPORT, storm);
		assertEquals(6, scn.GetStrength(messenger));
		assertEquals(6, scn.GetStrength(messenger2));

		// A weather condition borne by no site: each Messenger is +3 by its own text, and only once.
		scn.MoveCardsToSupportArea(urgency);
		assertInZone(Zone.SUPPORT, urgency);
		assertEquals(9, scn.GetStrength(messenger));
		assertEquals(9, scn.GetStrength(messenger2));
		assertEquals(5, scn.GetStrength(savage));
	}
}
