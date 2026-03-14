package com.gempukku.lotro.cards.unofficial.pc.errata.set07;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_07_091_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("faramir", "57_91");
					put("rohanman", "7_226");  // Enraged Horseman (Rohan Man, TWI 2, STR 5, VIT 3)
					put("gandalf", "1_364");   // Gandalf, The Grey Wizard (STR 7, VIT 4)
					put("pippin", "1_306");    // Pippin, Friend to Frodo (unbound Hobbit, STR 3, VIT 4)
					put("lurtz", "1_127");     // Lurtz, Servant of Isengard (STR 13, VIT 3)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FaramirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Faramir, Wizard's Pupil
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: <b>Ranger</b>.<br><b>Fellowship:</b> Play a [rohan] Man to heal Faramir.
		 *  <br><b>Skirmish:</b> Exert Faramir to make an unbound Hobbit strength +2.
		 *  <br><b>Skirmish:</b> Exert Gandalf to make Faramir unable to take wounds.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("faramir");

		assertEquals("Faramir", card.getBlueprint().getTitle());
		assertEquals("Wizard's Pupil", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void FellowshipPlayRohanManToHealFaramir() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var rohanman = scn.GetFreepsCard("rohanman");

		scn.MoveCompanionsToTable(faramir);
		scn.MoveCardsToHand(rohanman);
		scn.AddWoundsToChar(faramir, 1);

		scn.StartGame();

		assertEquals(1, scn.GetWoundsOn(faramir));

		// Use Faramir's fellowship ability -- play a Rohan Man to heal Faramir
		scn.FreepsUseCardAction(faramir);

		// Choose the Rohan Man to play
		scn.FreepsChooseCard(rohanman);

		// Faramir should be healed
		assertEquals(0, scn.GetWoundsOn(faramir));
	}

	@Test
	public void SkirmishExertFaramirForHobbitStrengthBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var pippin = scn.GetFreepsCard("pippin");
		var lurtz = scn.GetShadowCard("lurtz");

		scn.MoveCompanionsToTable(faramir, pippin);
		scn.MoveMinionsToTable(lurtz);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(pippin, lurtz);

		// During skirmish, use Faramir's ability to exert and boost Pippin
		scn.FreepsUseCardAction(faramir);
		// Choose Pippin as the unbound Hobbit to boost
		scn.FreepsChooseCard(pippin);

		// Faramir should have 1 wound from exertion
		assertEquals(1, scn.GetWoundsOn(faramir));
		// Pippin should have +2 strength (base 3 + 2 = 5)
		assertEquals(5, scn.GetStrength(pippin));
	}

	@Test
	public void SkirmishExertGandalfForCantTakeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var lurtz = scn.GetShadowCard("lurtz");

		scn.MoveCompanionsToTable(faramir, gandalf);
		scn.MoveMinionsToTable(lurtz);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(faramir, lurtz);

		// During skirmish actions, use Faramir's third ability (exert Gandalf)
		scn.FreepsUseCardAction(faramir);

		// Gandalf should have 1 wound from being exerted
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// Resolve skirmish -- Faramir (STR 7) vs Lurtz (STR 13), Faramir loses
		scn.PassSkirmishActions();

		// Faramir should have 0 wounds despite losing the skirmish
		// because CantTakeWounds prevents all wounds
		assertEquals(0, scn.GetWoundsOn(faramir));
	}
}
