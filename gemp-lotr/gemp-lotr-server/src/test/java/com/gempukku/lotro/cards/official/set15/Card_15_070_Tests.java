package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_070_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "15_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wall", "15_70");
					// Gondorian Knight: a [gondor] Man of strength 6 and vitality 3; Knight is its whole text
					put("knight1", "5_35");
					put("knight2", "5_35");
					// Uruk-hai Raiding Party: strength 9, so it beats a Knight without doubling him; Damage +1 is
					// its whole text
					put("party1", "1_158");
					put("party2", "1_158");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TremendousWallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Tremendous Wall
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Fortification</b>. Characters skirmishing bearer cannot take wounds from losing a skirmish involving bearer.<br><b>Skirmish:</b> Exert a [gondor] Man or spot 3 rangers to transfer this condition from your support area to a minion skirmishing a [gondor] Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Tremendous Wall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACharacterWhoLosesASkirmishToItsBearerTakesNoWoundForLosing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var wall = scn.GetFreepsCard("wall");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		scn.MoveCompanionsToTable(knight1, knight2);
		scn.MoveCardsToSupportArea(wall);

		var party1 = scn.GetShadowCard("party1");
		var party2 = scn.GetShadowCard("party2");

		scn.StartGame();
		scn.MoveMinionsToTable(party1, party2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{knight1, party1},
				new PhysicalCardImpl[]{knight2, party2});

		// The first Knight exerts to move the Wall onto the minion he is skirmishing.
		scn.FreepsResolveSkirmish(knight1);
		assertEquals(0, scn.GetWoundsOn(knight1));
		assertInZone(Zone.SUPPORT, wall);
		assertTrue(scn.FreepsActionAvailable(wall));
		scn.FreepsUseCardAction(wall);
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight1, knight2));
		scn.FreepsChooseCard(knight1);
		assertEquals(1, scn.GetWoundsOn(knight1));
		assertAttachedTo(wall, party1);

		// 6 against 9 and damage +1: he loses, and takes neither of the two wounds.  He has the one he
		// exerted for, where three would have killed him.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(knight1));
		assertInZone(Zone.FREE_CHARACTERS, knight1);
		assertEquals(0, scn.GetWoundsOn(party1));

		// The control: the minion the second Knight loses to bears no Wall, and he takes both.
		scn.FreepsResolveSkirmish(knight2);
		assertEquals(0, scn.GetWoundsOn(knight2));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(knight2));
		assertEquals(1, scn.GetWoundsOn(knight1));
		assertAttachedTo(wall, party1);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
