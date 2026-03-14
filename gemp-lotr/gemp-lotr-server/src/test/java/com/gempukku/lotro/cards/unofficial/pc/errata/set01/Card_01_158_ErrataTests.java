package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_158_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("raiding", "51_158");
					put("uruk1", "1_151"); // Uruk Lieutenant
					put("uruk2", "1_154"); // Uruk Brood
					put("uruk3", "1_147"); // Uruk Messenger
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukhaiRaidingPartyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk-hai Raiding Party
		 * Unique: false
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While you can spot 2 Uruk-hai, each Uruk-hai is strength +1.
		 * <br>While you can spot 4 Uruk-hai, this minion is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("raiding");

		assertEquals("Uruk-hai Raiding Party", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RaidingPartyGrantsStrengthBonusWith2UrukHaiAndFierceWith4() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var raiding = scn.GetShadowCard("raiding");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var uruk3 = scn.GetShadowCard("uruk3");

		// Just raiding party alone -- no bonuses
		scn.MoveMinionsToTable(raiding);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		// 1 Uruk-hai: base strength, no fierce
		assertEquals(9, scn.GetStrength(raiding));
		assertFalse(scn.HasKeyword(raiding, Keyword.FIERCE));

		// Add second Uruk-hai -- strength +1 to all Uruk-hai
		scn.MoveMinionsToTable(uruk1);
		assertEquals(10, scn.GetStrength(raiding));
		assertFalse(scn.HasKeyword(raiding, Keyword.FIERCE));

		// Add third Uruk-hai
		scn.MoveMinionsToTable(uruk2);
		assertEquals(10, scn.GetStrength(raiding));
		assertFalse(scn.HasKeyword(raiding, Keyword.FIERCE));

		// Add fourth Uruk-hai -- now raiding party gains fierce
		scn.MoveMinionsToTable(uruk3);
		assertEquals(10, scn.GetStrength(raiding));
		assertTrue(scn.HasKeyword(raiding, Keyword.FIERCE));
	}
}
