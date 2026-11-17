package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_303_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "1_303");

					put("sword", "1_299");      // Hobbit Sword: hand weapon, strength +2, bearer must be a Hobbit, no other text
					put("sword2", "1_299");
					// Merry's Dagger: unique [elven] hand weapon, strength +2, cost 1, bearer must be Merry; a skirmish
					// ability used only on purpose
					put("dagger", "9_18");
					put("pipe", "1_285");       // Bilbo's Pipe: a pipe, not a weapon, bearer must be a Hobbit; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8; a maneuver ability used only on purpose
					put("rsword", "1_112");     // Ranger's Sword: hand weapon, strength +2, Aragorn is damage +1
					put("smith", "3_60");       // Isengard Smith: Orc; "Regroup: Exert this minion to discard a weapon (or all Free Peoples possessions if you can spot 5 companions)."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Merry, From O'er the Brandywine
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: While Merry bears a weapon, he is strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merry");

		assertEquals("Merry", card.getBlueprint().getTitle());
		assertEquals("From O'er the Brandywine", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void IsStrength2MoreOnlyOnceHeBearsAWeaponHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var pipe = scn.GetFreepsCard("pipe");
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rsword = scn.GetFreepsCard("rsword");
		var sword2 = scn.GetFreepsCard("sword2");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(pipe, sword);
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.AttachCardsTo(aragorn, rsword);
		// Frodo, another Hobbit, bears a weapon all the while: Merry's bonus is not every Hobbit's.
		scn.AttachCardsTo(frodo, sword2);

		scn.StartGame();

		// Frodo is strength 3, +1 from The Ruling Ring, +2 from his Hobbit Sword.
		assertEquals(6, scn.GetStrength(frodo));

		// Aragorn's weapon is not Merry's.
		assertEquals(3, scn.GetStrength(merry));

		scn.FreepsPlayCard(pipe);
		scn.FreepsChooseCard(merry);
		assertAttachedTo(pipe, merry);
		// A pipe is not a weapon.
		assertEquals(3, scn.GetStrength(merry));

		// Frodo has his hand weapon: Merry is the only Hobbit who can bear this one, and nothing is asked.
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, merry);
		// 3 + 2 from the sword + 2 for bearing a weapon
		assertEquals(7, scn.GetStrength(merry));
		// Frodo is as he was.
		assertEquals(6, scn.GetStrength(frodo));
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AWeaponOfAnotherCultureMakesHimStrength2MoreAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var dagger = scn.GetFreepsCard("dagger");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(dagger);

		scn.StartGame();

		assertEquals(3, scn.GetStrength(merry));

		// Merry's Dagger is an [elven] hand weapon that Merry alone bears: it goes on him with nothing asked.
		scn.FreepsPlayCard(dagger);
		assertAttachedTo(dagger, merry);
		// 3 + 2 from the dagger + 2 for bearing a weapon
		assertEquals(7, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenHisWeaponIsDiscardedTheBonusIsGone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rsword = scn.GetFreepsCard("rsword");
		var smith = scn.GetShadowCard("smith");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.AttachCardsTo(merry, sword);
		scn.AttachCardsTo(aragorn, rsword);

		scn.StartGame();
		assertAttachedTo(sword, merry);
		assertEquals(7, scn.GetStrength(merry));
		scn.MoveMinionsToTable(smith);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertAttachedTo(sword, merry);
		assertEquals(7, scn.GetStrength(merry));
		assertTrue(scn.ShadowActionAvailable(smith));

		scn.ShadowUseCardAction(smith);
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, rsword));
		scn.ShadowChooseCard(sword);
		assertInZone(Zone.DISCARD, sword);
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
