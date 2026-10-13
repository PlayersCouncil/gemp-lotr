package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_115_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "18_115");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetVitalityScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("triumph", "18_115");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used
					// Uruk-hai whose whole text is damage +1
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FinalTriumphStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Final Triumph
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot your Uruk-hai skirmishing a companion to use vitality to resolve that skirmish instead of strength.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Final Triumph", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACompanionOfGreaterStrengthAndLessVitalityThanTheUrukHaiLosesTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetVitalityScenario();

		var triumph = scn.GetShadowCard("triumph");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(triumph);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);

		// On strength Aragorn wins, 8 against 5. On vitality he loses, 2 against 3.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(2, scn.GetVitality(aragorn));
		assertEquals(3, scn.GetVitality(savage));
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(triumph));
		scn.ShadowPlayCard(triumph);
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(triumph);

		scn.PassSkirmishActions();
		// The Uruk Savage wins and is not wounded. Aragorn takes a wound and a second for its damage +1, and
		// with 2 vitality left he dies of them.
		assertInZone(Zone.DEAD, aragorn);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnUrukHaiOfGreaterStrengthAndLessVitalityThanTheCompanionLosesTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetVitalityScenario();

		var triumph = scn.GetShadowCard("triumph");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(triumph);

		scn.StartGame();
		scn.MoveMinionsToTable(party);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, party);

		// On strength the Uruk-hai Raiding Party wins, 9 against 8. On vitality it loses, 3 against 4.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(party));
		assertEquals(4, scn.GetVitality(aragorn));
		assertEquals(3, scn.GetVitality(party));
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(triumph));
		scn.ShadowPlayCard(triumph);
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(triumph);

		scn.PassSkirmishActions();
		// Aragorn wins and is not wounded; the Raiding Party takes the one wound of a lost skirmish. (Had
		// Aragorn's strength been set against its vitality, 8 against 3 would have overwhelmed it.)
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, party);
		assertEquals(1, scn.GetWoundsOn(party));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
