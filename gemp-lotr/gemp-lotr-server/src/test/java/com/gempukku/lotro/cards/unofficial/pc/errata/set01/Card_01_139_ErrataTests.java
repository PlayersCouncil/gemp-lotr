package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_139_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");
					put("gimli", "1_13");
					put("arwen", "1_30");
					put("sam", "1_310");

					put("savagery", "51_139");
					put("uruk", "1_151");
					put("uruk2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SavagerytoMatchTheirNumbersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Savagery to Match Their Numbers
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an Uruk-hai strength +2.
		 * 	If you can spot 5 companions, also make it <b>fierce</b> and an additional strength +2 until the regroup phase.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("savagery");

		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SavageryAdds2StrengthFor1SkirmishIfThereAreLessThan5Companions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var savagery = scn.GetShadowCard("savagery");
		var uruk = scn.GetShadowCard("uruk");
		var uruk2 = scn.GetShadowCard("uruk2");
		scn.MoveCardsToHand(savagery);
		scn.MoveMinionsToTable(uruk, uruk2);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, uruk2}, new PhysicalCardImpl[]{aragorn,uruk});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(savagery));
		assertEquals(5, scn.GetStrength(uruk));
		scn.ShadowPlayCard(savagery);
		scn.ShadowChooseCard(uruk);
		assertEquals(7, scn.GetStrength(uruk));
		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(frodo);
		assertEquals(5, scn.GetStrength(uruk));
	}

	@Test
	public void SavageryAddsFierceAndPlus4Plus2IfMoreThan5Comps() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var savagery = scn.GetShadowCard("savagery");
		var uruk = scn.GetShadowCard("uruk");
		var uruk2 = scn.GetShadowCard("uruk2");
		scn.MoveCardsToHand(savagery);
		scn.MoveMinionsToTable(uruk, uruk2);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, uruk2}, new PhysicalCardImpl[]{aragorn,uruk});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(savagery));
		assertEquals(5, scn.GetStrength(uruk));
		scn.ShadowPlayCard(savagery);
		scn.ShadowChooseCard(uruk);
		assertEquals(7, scn.GetStrength(uruk));
		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(frodo);
		assertEquals(5, scn.GetStrength(uruk));
	}
}
