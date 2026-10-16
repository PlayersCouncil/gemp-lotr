package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_080_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("slits", "5_80");

					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; Valiant and no other text
					put("rider", "4_286");      // Rider of Rohan: a [rohan] Man companion; a skirmish ability only if mounted
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ALLY, home 4T; an ability used only on purpose
					// Merry, Master Holbytla: a [rohan] Hobbit companion, a Man only while 3 [rohan] companions or Eowyn can be
					// spotted; his abilities used only on purpose
					put("merry", "103_85");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion; a skirmish ability used only on purpose
					put("mount", "4_287");      // Rider's Mount: a Free Peoples possession on a [rohan] Man; text only at the start of his skirmish
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: a Free Peoples ARTIFACT on a [gondor] Man; a response used only on purpose

					put("savage", "1_151");     // Uruk Savage: an Uruk-hai minion, so that there is a maneuver phase; damage +1 only
					put("sword", "1_160");      // Uruk-hai Sword: a Shadow possession an Uruk-hai bears; a trigger on a skirmish won
					put("hides", "4_19");       // Hides: a Shadow possession in the support area; a response used only on purpose
					put("ram", "5_44");         // Battering Ram: an [isengard] machine (a condition) in the support area; abilities used only on purpose
					put("relics", "1_195");     // Relics of Moria: a Shadow condition in the support area that is no machine; a Shadow ability only
					put("weary", "4_212");      // Weary: a Shadow condition on a companion, no machine; works only on assignment to an [isengard] tracker
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArrowslitsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Arrow-slits
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Fortification</b>. Plays to your support area.
		 * <b>Maneuver:</b> Spot 2 [rohan] Men to discard a possession or a machine. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("slits");

		assertEquals("Arrow-slits", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACompanionAndAnAllyLetItDiscardAnyPossessionOrMachineChosenAndThenItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slits = scn.GetFreepsCard("slits");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var boromir = scn.GetFreepsCard("boromir");
		var mount = scn.GetFreepsCard("mount");
		var narsil = scn.GetFreepsCard("narsil");
		scn.MoveCardsToSupportArea(slits);
		scn.MoveCompanionsToTable(guard, boromir);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(boromir, narsil);

		var savage = scn.GetShadowCard("savage");
		var sword = scn.GetShadowCard("sword");
		var hides = scn.GetShadowCard("hides");
		var ram = scn.GetShadowCard("ram");
		var relics = scn.GetShadowCard("relics");
		var weary = scn.GetShadowCard("weary");
		scn.MoveCardsToSupportArea(hides, ram, relics);
		scn.AttachCardsTo(guard, weary);

		scn.StartGame();
		// Not a fellowship action.
		assertFalse(scn.FreepsActionAvailable(slits));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveMinionsToTable(savage);
		scn.AttachCardsTo(savage, sword);
		assertInZone(Zone.SUPPORT, slits, guma, hides, ram, relics);
		assertAttachedTo(mount, guard);
		assertAttachedTo(weary, guard);
		assertAttachedTo(narsil, boromir);
		assertAttachedTo(sword, savage);

		scn.SkipToPhase(Phase.MANEUVER);
		// The Guard (a companion) and Guma (an ally) are the two [rohan] Men.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(slits));
		scn.FreepsUseCardAction(slits);

		// Possessions of either side, borne or in a support area, and a machine; not a condition that is no
		// machine, wherever it lies, nor an artifact, nor Arrow-slits itself.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, hides, mount, ram));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(relics, weary, narsil, slits));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(sword);

		assertInDiscard(sword, slits);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.SUPPORT, hides, ram, relics, guma);
		assertAttachedTo(mount, guard);
		assertAttachedTo(weary, guard);
		assertAttachedTo(narsil, boromir);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AMachineChosenIsDiscardedAndArrowslitsWithIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slits = scn.GetFreepsCard("slits");
		var guard = scn.GetFreepsCard("guard");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCardsToSupportArea(slits);
		scn.MoveCompanionsToTable(guard, rider);

		var savage = scn.GetShadowCard("savage");
		var hides = scn.GetShadowCard("hides");
		var ram = scn.GetShadowCard("ram");
		scn.MoveCardsToSupportArea(hides, ram);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		assertInZone(Zone.SUPPORT, slits, hides, ram);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(slits));
		scn.FreepsUseCardAction(slits);

		assertTrue(scn.FreepsHasCardChoiceAvailable(hides, ram));
		scn.FreepsChooseCard(ram);

		assertInDiscard(ram, slits);
		assertInZone(Zone.SUPPORT, hides);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithOneRohanManBesideARohanHobbitAndAGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slits = scn.GetFreepsCard("slits");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToSupportArea(slits);
		scn.MoveCompanionsToTable(guard, merry, boromir);

		var savage = scn.GetShadowCard("savage");
		var sword = scn.GetShadowCard("sword");
		var ram = scn.GetShadowCard("ram");
		scn.MoveCardsToSupportArea(ram);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AttachCardsTo(savage, sword);
		assertInZone(Zone.SUPPORT, slits, ram);
		assertAttachedTo(sword, savage);

		scn.SkipToPhase(Phase.MANEUVER);
		// Two [rohan] companions: Merry is a Hobbit and no Man.  Boromir is a Man of [gondor].
		assertTrue(scn.IsRace(merry, Race.HOBBIT));
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(slits));
		assertInZone(Zone.SUPPORT, slits, ram);
		assertAttachedTo(sword, savage);
	}
}
