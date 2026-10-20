package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("heart", "1_10");
					put("heart2", "1_10");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3; a skirmish ability used only on purpose
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion, vitality 3; strength +2 against an Orc only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, vitality 3; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion, vitality 4; a maneuver ability used only on purpose
					// Ring of Retribution: a [dwarven] artifact a Dwarf bears, vitality +1, so that Gimli can carry 3
					// wounds; its Response answers only a skirmish won.
					put("ring", "9_9");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DwarvenHeartStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Dwarven Heart
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Bearer must be a Dwarf.<br>When you play this condition, heal bearer up to 2 times. At the start of each of your turns, exert bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("heart");

		assertEquals("Dwarven Heart", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnADwarfAndHealsBearerTwiceButNoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heart = scn.GetFreepsCard("heart");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ring = scn.GetFreepsCard("ring");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(heart);
		scn.MoveCompanionsToTable(gimli, farin, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(gimli, ring);

		scn.StartGame();
		// Gimli is vitality 4 with the Ring: three wounds tell two heals from three.
		scn.AddWoundsToChar(gimli, 3);
		scn.AddWoundsToChar(farin, 2);
		scn.AddWoundsToChar(grimir, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.FreepsRefreshDecision();

		assertInHand(heart);
		assertEquals(3, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(farin));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));

		assertTrue(scn.FreepsPlayAvailable(heart));
		scn.FreepsPlayCard(heart);
		// Dwarf companions of two titles and a Dwarf ally; not a Man, nor a Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);
		assertAttachedTo(heart, gimli);

		// Up to 2 times: one number, 0 to 2, filled in with 2 (Q-0325: B), and 2 named.
		assertTrue(scn.FreepsDecisionAvailable("Choose how many times to heal bearer"));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("2");

		// Two of his three wounds, and nobody else's.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(farin));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HealsBearerOnlyAsOftenAsChosenAndNotWhenAnotherHeartIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heart = scn.GetFreepsCard("heart");
		var heart2 = scn.GetFreepsCard("heart2");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCardsToHand(heart, heart2);
		scn.MoveCompanionsToTable(gimli, farin);

		scn.StartGame();
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(farin, 2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(farin));

		// Once: 1 named.
		scn.FreepsPlayCard(heart);
		scn.FreepsChooseCard(gimli);
		assertTrue(scn.FreepsDecisionAvailable("Choose how many times to heal bearer"));
		scn.FreepsChoose("1");
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(farin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// None: 0 named.  The second Heart's play is not the first Heart's own: Gimli is not healed again.
		scn.FreepsPlayCard(heart2);
		scn.FreepsChooseCard(farin);
		assertAttachedTo(heart2, farin);
		assertTrue(scn.FreepsDecisionAvailable("Choose how many times to heal bearer"));
		scn.FreepsChoose("0");
		assertEquals(2, scn.GetWoundsOn(farin));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertAttachedTo(heart, gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABearerWithOneWoundIsHealedOnceThoughTwoHealsAreNamed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heart = scn.GetFreepsCard("heart");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCardsToHand(heart);
		scn.MoveCompanionsToTable(gimli, farin);

		scn.StartGame();
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(farin, 1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(farin));

		scn.FreepsPlayCard(heart);
		scn.FreepsChooseCard(gimli);
		// The number is not held to his wounds, as on Elrond 3_13: 0 to 2, and 2 named.
		assertTrue(scn.FreepsDecisionAvailable("Choose how many times to heal bearer"));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");

		// His one wound is healed; the second heal goes to nobody else, and nothing more is asked.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(farin));
		assertFalse(scn.FreepsDecisionAvailable("heal bearer"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnUnwoundedBearerIsNotAskedToHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heart = scn.GetFreepsCard("heart");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCardsToHand(heart);
		scn.MoveCompanionsToTable(gimli, farin);

		scn.StartGame();
		scn.AddWoundsToChar(farin, 1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(farin));

		scn.FreepsPlayCard(heart);
		scn.FreepsChooseCard(gimli);
		assertAttachedTo(heart, gimli);

		// A heal would do nothing to him, and the wounded Farin is not his bearer: no number is asked.
		assertFalse(scn.FreepsDecisionAvailable("heal bearer"));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(farin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfEachOfItsOwnersTurnsBearerExertsAndNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heart = scn.GetFreepsCard("heart");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AttachCardsTo(gimli, heart);
		assertAttachedTo(heart, gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));

		// The owner's first turn starts: the bearer exerts, and the other Dwarf does not.
		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The opponent's turn starts: not one of "your turns".
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));

		// The owner's second turn starts: the bearer exerts again.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
