package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, not overwhelmed unless his strength is tripled
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its text answers its play, and it is cheated in
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Gimli, Son of Glóin
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: <b>Damage +1</b>.<br><b>Skirmish:</b> Exert Gimli to make him strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gimli");

		assertEquals("Gimli", card.getBlueprint().getTitle());
		assertEquals("Son of Glóin", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet()); 
	}

	@Test
	public void SkirmishAbilityExertsGimliToMakeHimStrengthPlus2ForThatSkirmishUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(gimli, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner1}, new PhysicalCardImpl[]{boromir, runner2});
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.FreepsActionAvailable(gimli));

		scn.FreepsUseCardAction(gimli);
		// Gimli and nobody else: no choice is asked, and Boromir is as he was.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(7, scn.GetStrength(boromir));

		scn.ShadowPass();
		scn.FreepsUseCardAction(gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(10, scn.GetStrength(gimli));

		// Vitality 3 with 2 wounds: exhausted, so he cannot pay again.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gimli));
		// Still in force in the last window before the skirmish is resolved.
		assertEquals(10, scn.GetStrength(gimli));
		scn.FreepsPass();

		// The next skirmish is a phase of its own: the +4 is gone.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
