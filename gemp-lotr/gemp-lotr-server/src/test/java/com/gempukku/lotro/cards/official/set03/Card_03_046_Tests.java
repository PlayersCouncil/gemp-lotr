package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_046_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sharp", "3_46");
					put("shards", "3_44");      // The Shards of Narsil: [gondor] artifact in the support area; fellowship abilities only
					put("memorial", "101_22");  // Gilraen's Memorial: another [gondor] artifact in the support area; a fellowship ability only, and no Aragorn here
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion, strength 3; a skirmish ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] companion, strength 6, damage +1; a skirmish ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] ally; his maneuver ability needs initiative and is used only on purpose
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its play trigger is never set off
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StillSharpStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Still Sharp
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Spot The Shards of Narsil to make a [gondor] companion strength +3 and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sharp");

		assertEquals("Still Sharp", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingTheShardsMakesAChosenGondorCompanionStrengthPlus3AndDamagePlus1ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sharp = scn.GetFreepsCard("sharp");
		var shards = scn.GetFreepsCard("shards");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var gimli = scn.GetFreepsCard("gimli");
		var merchant = scn.GetFreepsCard("merchant");
		var frodo = scn.GetRingBearer();
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(boromir, pippin, gimli);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(shards);
		scn.MoveCardsToHand(sharp);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, runner1}, new PhysicalCardImpl[]{gimli, runner2});
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(0, scn.GetKeywordCount(pippin, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(sharp));

		scn.FreepsPlayCard(sharp);
		// Any [gondor] companion, in this skirmish or in none; not a companion of another culture, nor a [gondor] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, frodo, merchant));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(boromir);

		assertEquals(10, scn.GetStrength(boromir));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(0, scn.GetKeywordCount(pippin, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertInDiscard(sharp);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still in force at the next decision of the same skirmish...
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(boromir));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		scn.FreepsPass();

		// ...and gone in the next skirmish.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
	}

	@Test
	public void CannotBePlayedWithAnotherGondorArtifactInPlaceOfTheShards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sharp = scn.GetFreepsCard("sharp");
		var shards = scn.GetFreepsCard("shards");
		var memorial = scn.GetFreepsCard("memorial");
		var boromir = scn.GetFreepsCard("boromir");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(memorial);
		scn.MoveCardsToHand(sharp);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, runner1);
		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sharp));

		scn.MoveCardsToSupportArea(shards);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(sharp));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
