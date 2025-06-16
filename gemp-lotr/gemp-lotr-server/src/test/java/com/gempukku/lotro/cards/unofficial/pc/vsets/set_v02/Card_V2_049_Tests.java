package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guthwine", "102_49");
					put("eowyn", "5_122");
					put("eomer", "4_267");
					put("condition", "6_97");

					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GuthwineStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Guthwine, Eomer's Blade
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be a [rohan] companion.
		* 	If bearer is Eomer, he is damage +1 and each time he wins a skirmish, you may reinforce a [rohan] token.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guthwine");

		assertEquals("Gúthwinë", card.getBlueprint().getTitle());
		assertEquals("Eomer's Blade", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void GuthwineCanBeBorneByAnyRohanCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var guthwine = scn.GetFreepsCard("guthwine");
		var eomer = scn.GetFreepsCard("eomer");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCardsToHand(guthwine);
		scn.MoveCompanionToTable(eomer, eowyn);

		scn.StartGame();
		scn.FreepsPlayCard(guthwine);

		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer));
		assertTrue(scn.FreepsHasCardChoiceAvailable(eowyn));

		scn.FreepsChooseCard(eowyn);
		assertEquals(Zone.ATTACHED, guthwine.getZone());
		assertEquals(eowyn, guthwine.getAttachedTo());
	}

	@Test
	public void GuthwineMakesEomerBearerDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var guthwine = scn.GetFreepsCard("guthwine");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCardsToHand(guthwine);
		scn.MoveCompanionToTable(eomer);

		scn.StartGame();

		assertFalse(scn.HasKeyword(eomer, Keyword.DAMAGE));

		scn.FreepsPlayCard(guthwine);

		assertEquals(Zone.ATTACHED, guthwine.getZone());
		assertEquals(eomer, guthwine.getAttachedTo());
		assertTrue(scn.HasKeyword(eomer, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(eomer, Keyword.DAMAGE));
	}

	@Test
	public void GuthwineDoesNotMakeNonEomerBearerDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var guthwine = scn.GetFreepsCard("guthwine");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCardsToHand(guthwine);
		scn.MoveCompanionToTable(eowyn);

		scn.StartGame();

		assertFalse(scn.HasKeyword(eowyn, Keyword.DAMAGE));

		scn.FreepsPlayCard(guthwine);

		assertEquals(Zone.ATTACHED, guthwine.getZone());
		assertEquals(eowyn, guthwine.getAttachedTo());
		assertFalse(scn.HasKeyword(eowyn, Keyword.DAMAGE));
	}

	@Test
	public void GuthwineReinforcesRohanTokenWhenEomerWinsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var guthwine = scn.GetFreepsCard("guthwine");
		var eomer = scn.GetFreepsCard("eomer");
		var condition = scn.GetFreepsCard("condition");
		scn.MoveCompanionToTable(eomer);
		scn.AttachCardsTo(eomer, guthwine);
		scn.MoveCardsToSupportArea(condition);
		scn.AddTokensToCard(condition, 1);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(eomer, runner);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetCultureTokensOn(condition));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetCultureTokensOn(condition));
	}

	@Test
	public void GuthwineDoesNotReinforceRohanTokenWhenNonEomerWinsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var guthwine = scn.GetFreepsCard("guthwine");
		var eowyn = scn.GetFreepsCard("eowyn");
		var condition = scn.GetFreepsCard("condition");
		scn.MoveCompanionToTable(eowyn);
		scn.AttachCardsTo(eowyn, guthwine);
		scn.MoveCardsToSupportArea(condition);
		scn.AddTokensToCard(condition, 1);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(eowyn, runner);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetCultureTokensOn(condition));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		assertEquals(1, scn.GetCultureTokensOn(condition));
	}
}
