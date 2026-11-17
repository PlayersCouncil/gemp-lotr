package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_06_043_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("listening", "6_43");
					put("smeagol", "6_45");     // Sméagol, Poor Creature: [gollum] companion, strength 3, vitality 4; a skirmish ability used only on purpose
					put("smeagol2", "103_116"); // Smeagol, Once Like Us: the [shire] Sméagol, strength 3, vitality 4; an assignment ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a wounded companion beside Sméagol; his abilities are used only on purpose

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger is not met when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NotListeningStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Not Listening
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If Sméagol wins a skirmish, heal him and remove 2 burdens.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("listening");

		assertEquals("Not Listening", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenSmeagolWinsASkirmishItHealsHimAndRemovesTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var listening = scn.GetFreepsCard("listening");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(smeagol, sam);
		scn.MoveCardsToHand(listening);
		// 3 + 5 against the Savage's 5: he wins without overwhelming it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, smeagol, null, 5));

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AddWoundsToChar(smeagol, 2);
		scn.AddWoundsToChar(sam, 1);
		scn.AddBurdens(3);
		assertInHand(listening);
		assertEquals(2, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(3, scn.GetBurdens());
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, savage);
		scn.FreepsResolveSkirmish(smeagol);
		// Nobody has won anything yet.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(listening));

		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(listening));
		scn.FreepsChooseOptionalTrigger(listening);

		// One wound of his two healed; 3 burdens less 2.
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetBurdens());
		// "Him" is the winner: the wounded companion beside him, and the Ring-bearer, are not healed.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.DISCARD, listening);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenTheShireSmeagolWinsItIsPlayableAndRemovesTheOneBurdenThereIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var listening = scn.GetFreepsCard("listening");
		var smeagol = scn.GetFreepsCard("smeagol2");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(listening);
		// 3 + 5 against the Savage's 5
		scn.ApplyAdHocModifier(new StrengthModifier(null, smeagol, null, 5));

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AddWoundsToChar(smeagol, 1);
		scn.AddBurdens(1);
		assertInHand(listening);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, savage);
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(listening));
		scn.FreepsChooseOptionalTrigger(listening);

		// Healed; the one burden there is removed.
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DISCARD, listening);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotPlayableWhenACompanionWhoIsNoSmeagolWinsOrWhenSmeagolLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var listening = scn.GetFreepsCard("listening");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(listening);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddBurdens(2);
		assertInHand(listening);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(savage, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{smeagol, runner});

		// 8 against 5: Aragorn, who is no Sméagol, wins.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(listening));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 3 against 5: Sméagol loses, and lives through his wound.
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(listening));
		assertInHand(listening);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
