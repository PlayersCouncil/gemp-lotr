package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static org.junit.Assert.*;

public class Card_92_035_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Aragorn, Ranger of the North (1_89): unique Gondor Man companion, vitality 4
		put("aragorn", "1_89");
		// Second copy for discard-to-heal
		put("aragornCopy", "1_89");
		// Boromir, Lord of Gondor (1_96): unique Gondor Man companion, vitality 3
		put("boromir", "1_96");
		// Athelas (1_94): Gondor possession, fellowship action: discard self to heal a companion
		put("athelas", "1_94");
		// Nenya, Ring of Adamant (9_20): Maneuver: heal 2 companions. Requires Galadriel but we cheat it.
		put("nenya", "9_20");
		// Ulaire Enquea, Lieutenant of Morgul (1_231): Nazgul minion to reach maneuver phase
		put("enquea", "1_231");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_35", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_35"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_35
		 * Type: MetaSite
		 * Game Text: While at a sanctuary, you may heal no more than one wound from each character.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_35", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void SanctuaryHealingLimitedToOnePerCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(boromir, 2);

		scn.StartGame();

		scn.SkipToSite(3);

		// Sanctuary healing at start of turn; 5 heals available.
		// First heal on Aragorn — should work
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Second heal — Aragorn should no longer be chooseable (CantHeal applied),
		// but Boromir should be
		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));

		// Third heal — both already healed once, neither should be chooseable.
		// Remaining sanctuary heals should fizzle/skip.
		// We should land in fellowship phase actions.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AthelasCantHealAlreadyHealedCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas);
		scn.AddWoundsToChar(aragorn, 2);

		scn.StartGame();

		scn.SkipToSite(3);

		// Sanctuary healing heals Aragorn once
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasCardChoiceAvailable(aragorn));

		// Now in fellowship phase. Athelas should not be able to heal Aragorn
		// (already healed once at this sanctuary).
		assertTrue(scn.FreepsActionAvailable(athelas));
		scn.FreepsUseCardAction(athelas);
		scn.FreepsChoose("heal");
		assertInDiscard(athelas);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasCardChoiceAvailable(aragorn));

		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardToHealFizzlesAtSanctuary() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var aragornCopy = scn.GetFreepsCard("aragornCopy");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(aragornCopy);
		scn.AddWoundsToChar(aragorn, 2);

		scn.StartGame();
		scn.SkipToSite(3);

		// Sanctuary healing heals Aragorn once
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Discard-to-heal action should be available (it checks wounds > 0, not canBeHealed)
		assertTrue(scn.FreepsHasDiscardToHealAvailable());
		scn.FreepsDiscardToHeal(aragornCopy);

		// Heal fizzles but card is still discarded (cost paid)
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HealingWorksAgainAfterLeavingSanctuary() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var frodo = scn.GetRingBearer();
		var nenya = scn.GetFreepsCard("nenya");
		var enquea = scn.GetShadowCard("enquea");

		scn.AttachCardsTo(frodo, nenya);
		scn.AddWoundsToChar(frodo, 2);

		scn.StartGame();

		scn.SkipToSite(3);
		scn.MoveMinionsToTable(enquea);

		// Sanctuary healing heals Frodo once
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));

		// Pass fellowship phase; auto-moves to site 4 (non-sanctuary)
		scn.FreepsPassCurrentPhaseAction();

		scn.SkipToPhase(Phase.MANEUVER);

		// Use Nenya to heal at non-sanctuary — should not be blocked
		scn.FreepsUseCardAction(nenya);

		assertEquals(0, scn.GetWoundsOn(frodo));
	}

	@Test
	public void OpponentNotAffected() throws DecisionResultInvalidException, CardNotFoundException {
		// When Shadow owns the modifier, Freeps healing should not be restricted
		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(aragorn, 2);

		scn.StartGame();
		scn.SkipToSite(3);

		// Sanctuary healing — Aragorn should be healable twice (no restriction)
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.FreepsChooseCard(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}
}
