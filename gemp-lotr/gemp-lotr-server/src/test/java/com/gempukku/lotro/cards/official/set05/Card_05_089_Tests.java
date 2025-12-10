package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static org.junit.Assert.*;

public class Card_05_089_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("helm", "5_89");          // Rohirrim Helm
					put("guard", "5_83");         // Household Guard - [Rohan] Man

					put("grishnakh", "5_100");    // Grishnakh - [Sauron] Orc
					put("stalker", "7_307");      // Orc Stalker - [Sauron] Orc
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimHelmStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Rohirrim Helm
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Helm
		 * Game Text: Bearer must be a [rohan] Man.<br><b>Skirmish:</b> Discard this possession to cancel a skirmish involving bearer. A minion in this skirmish may exert to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("helm");

		assertEquals("Rohirrim Helm", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HELM));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}



	@Test
	public void RohirrimHelmCancelsSkirmishWhenShadowDeclinesExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var guard = scn.GetFreepsCard("guard");
		var grishnakh = scn.GetShadowCard("grishnakh");
		var stalker = scn.GetShadowCard("stalker");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, helm);
		scn.MoveMinionsToTable(grishnakh, stalker);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, grishnakh);
		scn.ShadowAssignToMinions(guard, stalker);
		scn.FreepsResolveSkirmish(guard);

		// Use Rohirrim Helm to cancel skirmish
		assertTrue(scn.FreepsActionAvailable(helm));
		scn.FreepsUseCardAction(helm);

		// Helm discards as cost
		assertInDiscard(helm);

		// Shadow is offered chance to exert a minion to prevent
		assertTrue(scn.ShadowDecisionAvailable("Would you like to exert"));
		scn.ShadowChooseNo();

		// Skirmish should be cancelled
		assertFalse(scn.IsCharSkirmishing(guard));
		assertFalse(scn.IsCharSkirmishing(grishnakh));
		assertFalse(scn.IsCharSkirmishing(stalker));
	}
}
