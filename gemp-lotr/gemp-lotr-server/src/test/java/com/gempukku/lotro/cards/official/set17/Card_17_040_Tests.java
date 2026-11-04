package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beast", "17_40");
					put("beast2", "17_40");
					// Sunland Skirmisher: a [men] Man; his texts read [men] minions stacked on [men] possessions
					// (none here)
					put("man", "17_57");
					// Squad of Haradrim, Easterling Runner: [men] Men of two titles; their abilities are not
					// offered here (the Runner's wants a culture token to remove)
					put("squad", "11_99");
					put("runner", "13_90");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is no Man (a Hobbit)
					put("ted", "18_75");
					// Dunlending Brigand: a [dunland] Man with no text
					put("brigand", "4_10");

					// Four companions beside Frodo, so that five threats can lie on the dead pile
					put("aragorn", "1_89");
					put("gimli", "1_13");
					put("legolas", "1_50");
					put("boromir", "1_97");
					// Oathbreaker: a [gondor] Wraith companion; "To play, add a threat"
					put("oathbreaker", "8_41");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeastofWarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Beast of War
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 3
		 * Game Text: Bearer must be a [men] Man.<br>While you can spot a threat, bearer is <b>fierce</b>.<br>While you can spot 3 threats, bearer is <b>damage +1</b>.<br>While you can spot 5 threats, you may play this from your discard pile.
		*/

		var scn = GetScenario();

		var beast = scn.GetFreepsCard("beast");

		assertEquals("Beast of War", beast.getBlueprint().getTitle());
		assertNull(beast.getBlueprint().getSubtitle());
		assertFalse(beast.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, beast.getBlueprint().getSide());
		assertEquals(Culture.MEN, beast.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, beast.getBlueprint().getCardType());
		assertTrue(beast.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(3, beast.getBlueprint().getTwilightCost());
		assertEquals(3, beast.getBlueprint().getStrength());
	}

	// F-0060 (Ketura, 2026-10-07: "clearly an implementation error"): the definition let it be played from the
	// discard pile at 3 threats; the text says 5.
	@Test
	public void IsPlayedFromTheDiscardPileOnlyWhileFiveThreatsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beast = scn.GetShadowCard("beast");
		var beast2 = scn.GetShadowCard("beast2");
		var man = scn.GetShadowCard("man");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("legolas"), scn.GetFreepsCard("boromir"));
		scn.MoveCardsToDiscard(beast);
		scn.MoveCardsToHand(beast2);

		scn.StartGame();
		scn.MoveMinionsToTable(man);
		scn.SkipToPhase(Phase.SHADOW);
		scn.SetTwilight(10);

		// Four threats, with five companions to hold five: one short.
		scn.AddThreats(4);
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetThreats());
		assertInZone(Zone.DISCARD, beast);
		assertFalse(scn.ShadowActionAvailable(beast));
		// The threats are a condition of the play from the pile alone: a copy in hand is playable.
		assertTrue(scn.ShadowPlayAvailable(beast2));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(5, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(beast));

		scn.ShadowUseCardAction(beast);

		assertAttachedTo(beast, man);
		assertEquals(5, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBeBorneByMenMenOfTwoTitlesAndNotByAMenHobbitOrADunlandMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beast = scn.GetShadowCard("beast");
		var squad = scn.GetShadowCard("squad");
		var runner = scn.GetShadowCard("runner");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(beast);
		assertInHand(beast);

		scn.StartGame();
		scn.MoveMinionsToTable(squad, runner, ted, brigand);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(beast));
		scn.ShadowPlayCard(beast);

		// Two [men] Men; not a [men] minion who is a Hobbit, nor a Man of [dunland].
		assertTrue(scn.ShadowHasCardChoiceAvailable(squad, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ted, brigand));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner);

		assertAttachedTo(beast, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerIsFierceAtOneThreatAndDamagePlus1AtThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beast = scn.GetShadowCard("beast");
		var squad = scn.GetShadowCard("squad");
		var runner = scn.GetShadowCard("runner");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("legolas"), scn.GetFreepsCard("boromir"));
		scn.MoveCardsToHand(oathbreaker);

		scn.StartGame();
		// The minions are laid in the fellowship phase only to read the standing text.
		scn.MoveMinionsToTable(squad, runner);
		scn.AttachCardsTo(squad, beast);
		// A burden, the token that is no threat.
		scn.AddBurdens(1);
		scn.FreepsRefreshDecision();

		assertAttachedTo(beast, squad);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.HasKeyword(squad, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(squad, Keyword.DAMAGE));

		// Oathbreaker's own cost adds the first threat: the bearer is fierce, the [men] Man beside him is not.
		scn.FreepsPlayCard(oathbreaker);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.HasKeyword(squad, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(squad, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));

		// Two threats: one short of the damage bonus.
		scn.AddThreats(1);
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetKeywordCount(squad, Keyword.DAMAGE));

		// Three: the bearer is damage +1 and still fierce; the other [men] Man gains neither.
		scn.AddThreats(1);
		assertEquals(3, scn.GetThreats());
		assertEquals(1, scn.GetKeywordCount(squad, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(squad, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(runner, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
