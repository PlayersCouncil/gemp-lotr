package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("friends", "102_12");
					put("aragorn", "4_109");
					put("gimli", "4_49");
					put("legolas", "4_74");

					put("uruk", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void YourFriendsAreWithYouStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Your Friends Are With You
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time your Man, Elf, or Dwarf wins a skirmish, make your Man, Elf, or Dwarf of another race strength +1 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("friends");

		assertEquals("Your Friends Are With You", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FriendsLetsAragornPumpGimliOrLegolas() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToSupportArea(friends);
		scn.MoveCompanionsToTable(aragorn, gimli, legolas);

		var uruk = scn.GetShadowCard("uruk");
		scn.MoveMinionsToTable(uruk);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, uruk);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(legolas));

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas));
		scn.FreepsChooseCard(gimli);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(legolas));

		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(legolas));
	}

	@Test
	public void FriendsLetsGimliPumpAragornOrLegolas() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToSupportArea(friends);
		scn.MoveCompanionsToTable(aragorn, gimli, legolas);

		var uruk = scn.GetShadowCard("uruk");
		scn.MoveMinionsToTable(uruk);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.RemoveWoundsFromChar(uruk, 1); //else the damage+1 gimli will kill him

		scn.FreepsAssignToMinions(gimli, uruk);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(legolas));

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas));
		scn.FreepsChooseCard(legolas);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(7, scn.GetStrength(legolas));

		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(legolas));
	}

	@Test
	public void FriendsLetsLegolasPumpAragornOrGimli() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToSupportArea(friends);
		scn.MoveCompanionsToTable(aragorn, gimli, legolas);

		var uruk = scn.GetShadowCard("uruk");
		scn.MoveMinionsToTable(uruk);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, uruk);
		scn.FreepsResolveSkirmish(legolas);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(legolas));

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli));
		scn.FreepsChooseCard(aragorn);

		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(legolas));

		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(legolas));
	}
}
