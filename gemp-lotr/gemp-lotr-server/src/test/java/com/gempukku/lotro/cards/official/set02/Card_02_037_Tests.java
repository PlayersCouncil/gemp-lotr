package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sentinels1", "2_37");
					put("sentinels2", "2_37");

					put("boromir", "1_96");     // Boromir, Lord of Gondor: [gondor] Man; not overwhelmed unless his strength is tripled
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit; his skirmish ability is used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] companion; his skirmish ability is used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the only [gondor] ally; his maneuver ability is used only on purpose

					// Uruk Pursuer: fierce, strength 5, vitality 3; its texts are about unbound Hobbits being
					// discarded or killed, and none is
					put("pursuer", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SentinelsofNumenorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Sentinels of Númenor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a [gondor] companion strength +2 (or +4 if in a fierce skirmish).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sentinels1");

		assertEquals("Sentinels of Númenor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAGondorCompanionPlusTwoAgainstAFierceMinionAndPlusFourInTheFierceSkirmishForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinels1 = scn.GetFreepsCard("sentinels1");
		var sentinels2 = scn.GetFreepsCard("sentinels2");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var gimli = scn.GetFreepsCard("gimli");
		var merchant = scn.GetFreepsCard("merchant");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(boromir, pippin, gimli);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(sentinels1, sentinels2);
		assertInHand(sentinels1, sentinels2);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);

		// The assignment phase, next door to the skirmish: not playable.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sentinels1));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(boromir, pursuer);
		scn.FreepsResolveSkirmish(boromir);

		// Against a fierce minion, but not in a fierce skirmish.
		assertFalse(scn.InFierceRound());
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.FreepsPlayAvailable(sentinels1));

		scn.FreepsPlayCard(sentinels1);
		// [gondor] companions of two races, one in no skirmish; not a companion of another culture, the [gondor]
		// ally or the Ring-bearer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, merchant, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(boromir);

		assertInDiscard(sentinels1);
		assertEquals(9, scn.GetStrength(boromir));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Boromir wins, 9 to 5; the skirmish is over, and so is the +2.
		scn.PassCurrentPhaseActions();
		assertTrue(scn.InFierceRound());
		assertEquals(7, scn.GetStrength(boromir));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, pursuer);
		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.InFierceRound());
		assertEquals(7, scn.GetStrength(boromir));
		scn.FreepsPlayCard(sentinels2);
		scn.FreepsChooseCard(boromir);

		assertInDiscard(sentinels2);
		// 7 + 4 in a fierce skirmish
		assertEquals(11, scn.GetStrength(boromir));
		assertEquals(3, scn.GetStrength(pippin));

		// Boromir wins again; the fierce skirmish is over, and so is the +4.
		scn.PassCurrentPhaseActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(7, scn.GetStrength(boromir));
	}

	@Test
	public void AGondorCompanionInNoSkirmishIsOnlyPlusTwoDuringAFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinels1 = scn.GetFreepsCard("sentinels1");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(boromir, pippin);
		scn.MoveCardsToHand(sentinels1);
		assertInHand(sentinels1);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, pursuer);
		scn.FreepsResolveSkirmish(boromir);
		// Boromir wins, 7 to 5, and the Pursuer lives to fight again.
		scn.PassCurrentPhaseActions();

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, pursuer);
		scn.FreepsResolveSkirmish(boromir);

		// Boromir is in the fierce skirmish; Pippin, beside him, is in no skirmish.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, pursuer));
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertEquals(3, scn.GetStrength(pippin));

		scn.FreepsPlayCard(sentinels1);
		scn.FreepsChooseCard(pippin);

		assertInDiscard(sentinels1);
		// 3 + 2: Pippin is not in a fierce skirmish.
		assertEquals(5, scn.GetStrength(pippin));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
