package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_046_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("end", "102_46");
					put("fortress", "4_276");
					put("none", "6_97");
					put("doom", "8_86");
					put("vhaldir", "102_8");
					put("vgimli", "55_7");
					put("veowyn", "4_270");
					put("veomer", "6_92");

					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AnEndWorthyofRemembranceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: An End Worthy of Remembrance
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot X [rohan] conditions and remove 2 tokens from each to make 3 valiant companions strength +X until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("end");

		assertEquals("An End Worthy of Remembrance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AnEndWorthyofRemembrancePumps3ValiantCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var end = scn.GetFreepsCard("end");
		var fortress = scn.GetFreepsCard("fortress");
		var none = scn.GetFreepsCard("none");
		var doom = scn.GetFreepsCard("doom");
		var vhaldir = scn.GetFreepsCard("vhaldir");
		var vgimli = scn.GetFreepsCard("vgimli");
		var veowyn = scn.GetFreepsCard("veowyn");
		var veomer = scn.GetFreepsCard("veomer");
		scn.MoveCardsToHand(end);
		scn.MoveCardsToSupportArea(fortress, none, doom);
		scn.MoveCompanionsToTable(vhaldir, vgimli, veowyn, veomer);
		scn.AddTokensToCard(fortress, 3);
		scn.AddTokensToCard(none, 3);
		scn.AddTokensToCard(doom, 3);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(vgimli, runner);
		scn.FreepsResolveSkirmish(vgimli);

		assertEquals(3, scn.GetCultureTokensOn(fortress));
		assertEquals(3, scn.GetCultureTokensOn(none));
		assertEquals(3, scn.GetCultureTokensOn(doom));
		assertEquals(6, scn.GetStrength(vhaldir));
		assertEquals(6, scn.GetStrength(vgimli));
		assertEquals(6, scn.GetStrength(veowyn));
		assertEquals(7, scn.GetStrength(veomer));

		assertTrue(scn.FreepsPlayAvailable(end));
		scn.FreepsPlayCard(end);
		scn.FreepsChooseCards(fortress, none, doom);

		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(vgimli, vhaldir, veowyn);
		assertEquals(1, scn.GetCultureTokensOn(fortress));
		assertEquals(1, scn.GetCultureTokensOn(none));
		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertEquals(9, scn.GetStrength(vhaldir));
		assertEquals(9, scn.GetStrength(vgimli));
		assertEquals(9, scn.GetStrength(veowyn));
		assertEquals(7, scn.GetStrength(veomer));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(6, scn.GetStrength(vhaldir));
		assertEquals(6, scn.GetStrength(vgimli));
		assertEquals(6, scn.GetStrength(veowyn));
		assertEquals(7, scn.GetStrength(veomer));
	}

	@Test
	public void AnEndWorthyofRemembranceCannotPlayIfNoConditionsWith2RohanTokens() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var end = scn.GetFreepsCard("end");
		var fortress = scn.GetFreepsCard("fortress");
		var none = scn.GetFreepsCard("none");
		var doom = scn.GetFreepsCard("doom");
		scn.MoveCardsToHand(end);
		scn.MoveCardsToSupportArea(fortress, none, doom);
		scn.AddTokensToCard(fortress, 1);
		scn.AddTokensToCard(none, 1);
		scn.AddTokensToCard(doom, 1);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, runner);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(1, scn.GetCultureTokensOn(fortress));
		assertEquals(1, scn.GetCultureTokensOn(none));
		assertEquals(1, scn.GetCultureTokensOn(doom));

		assertFalse(scn.FreepsPlayAvailable(end));
	}

	@Test
	public void AnEndWorthyofRemembrancePumps3ValiantCompanionsPlusOneIfOnlyOneConditionUsed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var end = scn.GetFreepsCard("end");
		var fortress = scn.GetFreepsCard("fortress");
		var none = scn.GetFreepsCard("none");
		var doom = scn.GetFreepsCard("doom");
		var vhaldir = scn.GetFreepsCard("vhaldir");
		var vgimli = scn.GetFreepsCard("vgimli");
		var veowyn = scn.GetFreepsCard("veowyn");
		scn.MoveCardsToHand(end);
		scn.MoveCardsToSupportArea(fortress, none, doom);
		scn.MoveCompanionsToTable(vhaldir, vgimli, veowyn);
		scn.AddTokensToCard(fortress, 3);
		scn.AddTokensToCard(none, 3);
		scn.AddTokensToCard(doom, 3);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(vgimli, runner);
		scn.FreepsResolveSkirmish(vgimli);

		assertEquals(3, scn.GetCultureTokensOn(fortress));
		assertEquals(3, scn.GetCultureTokensOn(none));
		assertEquals(3, scn.GetCultureTokensOn(doom));
		assertEquals(6, scn.GetStrength(vhaldir));
		assertEquals(6, scn.GetStrength(vgimli));
		assertEquals(6, scn.GetStrength(veowyn));

		assertTrue(scn.FreepsPlayAvailable(end));
		scn.FreepsPlayCard(end);
		scn.FreepsChooseCards(fortress);

		assertEquals(1, scn.GetCultureTokensOn(fortress));
		assertEquals(3, scn.GetCultureTokensOn(none));
		assertEquals(3, scn.GetCultureTokensOn(doom));
		assertEquals(7, scn.GetStrength(vhaldir));
		assertEquals(7, scn.GetStrength(vgimli));
		assertEquals(7, scn.GetStrength(veowyn));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(6, scn.GetStrength(vhaldir));
		assertEquals(6, scn.GetStrength(vgimli));
		assertEquals(6, scn.GetStrength(veowyn));
	}
}
