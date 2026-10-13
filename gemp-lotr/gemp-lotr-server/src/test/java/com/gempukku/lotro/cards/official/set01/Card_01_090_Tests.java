package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_090_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "1_90");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, cost 4, vitality 4, no archer; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion who is not Aragorn, no archer; a skirmish ability never used
					put("legolas", "1_50");     // Legolas, Greenleaf: an archer companion; his own Archery ability is never used
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, vitality 1
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, vitality 3, Damage +1 and no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRohanAragornScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "1_90");
					// Aragorn, Defender of Rohan: the only Aragorn who is not [gondor]; his hunter needs the Ring-bearer assigned
					put("aragorn", "17_93");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion who is not Aragorn
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornsBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Aragorn's Bow
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Ranged Weapon
		 * Game Text: Bearer must be Aragorn.
		 * He is an archer.
		 * Archery: Exert Aragorn to wound a minion; Aragorn does not add to the fellowship archery total.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bow");

		assertEquals("Aragorn's Bow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnlyOnAragornAndMakesHimAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(aragorn, bow);

		scn.StartGame();

		// Frodo and Boromir, and no Aragorn: nobody may bear it.
		assertFalse(scn.FreepsPlayAvailable(bow));

		scn.FreepsPlayCard(aragorn);
		assertTrue(scn.FreepsPlayAvailable(bow));
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));

		// Aragorn is the only bearer it may have, so nobody is asked.
		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, aragorn);
		assertTrue(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(boromir, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysOnAragornDefenderOfRohanAndMakesHimAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRohanAragornScenario();

		var bow = scn.GetFreepsCard("bow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(bow);

		scn.StartGame();

		assertEquals(Culture.ROHAN, aragorn.getBlueprint().getCulture());
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertTrue(scn.FreepsPlayAvailable(bow));

		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, aragorn);
		assertTrue(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(boromir, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArcheryAbilityExertsAragornToWoundAMinionAndHeAddsNothingToTheArcheryTotalForThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.AttachCardsTo(aragorn, bow);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToPhase(Phase.ARCHERY);

		// Aragorn, an archer by the Bow, and Legolas each add 1.
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(bow));

		scn.FreepsUseCardAction(bow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, legolas, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(legolas));
		// Aragorn no longer adds; Legolas still does.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// The one archery wound of the fellowship is placed by the Shadow player, on the Uruk-hai.
		scn.SkipToArcheryWounds();
		scn.ShadowChooseCard(savage);
		assertEquals(2, scn.GetWoundsOn(savage));

		// Once the archery phase is over, Aragorn counts again.
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWhileAragornIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.AttachCardsTo(aragorn, bow);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(aragorn, 3);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bow));
		// Legolas, who could exert, is offered his own.
		assertTrue(scn.FreepsActionAvailable(legolas));
	}
}
