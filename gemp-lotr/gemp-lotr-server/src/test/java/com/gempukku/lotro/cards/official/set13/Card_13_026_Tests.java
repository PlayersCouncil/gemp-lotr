package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_026_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "13_26");
					put("bow2", "13_26");
					// Legolas, Greenleaf: Elf archer companion; an archery ability used only on purpose
					put("legolas", "1_50");
					// Rumil, Silvan Elf: Elf companion, strength 6, who is strength +1 and gains archer while a wounded
					// minion can be spotted; an archery ability (exert another [elven] hunter) used only on purpose
					put("rumil", "17_12");
					// Orophin, Lorien Bowman: Elf archer ally, home 6F; "Archery: Exert Orophin to wound an Uruk-hai"
					put("orophin", "1_56");
					// Calaglin, Elf of Lorien: Elf archer ally, home 6F; a regroup ability used only on purpose
					put("calaglin", "3_10");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf who is no archer
					// Garrison of Gondor: an archer who is no Elf ([gondor] Man companion); defender only beside a
					// fortification (none)
					put("garrison", "8_36");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] companion; a skirmish ability used only on purpose
					// Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("soldier", "1_271");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TakeUptheBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Take Up the Bow
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 6
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Toil 2</b>. <helper>(For each [elven] character you exert when playing this, its twilight cost is -2.)</helper><br><b>Archery:</b> Discard this from play and spot 3 Elf archers to discard a minion from play. Each of those archers loses <b>archer</b> and cannot gain archer.
		*/

		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");

		assertEquals("Take Up the Bow", bow.getBlueprint().getTitle());
		assertNull(bow.getBlueprint().getSubtitle());
		assertFalse(bow.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, bow.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, bow.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, bow.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(bow, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(bow, Keyword.TOIL));
		assertTrue(scn.HasKeyword(bow, Keyword.SUPPORT_AREA));
		assertEquals(6, bow.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingTwoElvenCharactersForToilTakesFourOffItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(bow);

		scn.StartGame();
		assertInHand(bow);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(orophin));

		assertTrue(scn.FreepsPlayAvailable(bow));
		scn.FreepsPlayCard(bow);
		// A Free Peoples card adds its cost: no exertion is owed, and any [elven] character may exert, an ally too.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, frodo));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(legolas, orophin);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(gimli));
		// 6 - 2 - 2
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, bow);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SpottingThreeElfArchersDiscardsAMinionAndThoseThreeLoseArcherAndCannotGainItForThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var bow2 = scn.GetFreepsCard("bow2");
		var legolas = scn.GetFreepsCard("legolas");
		var rumil = scn.GetFreepsCard("rumil");
		var orophin = scn.GetFreepsCard("orophin");
		var calaglin = scn.GetFreepsCard("calaglin");
		var arwen = scn.GetFreepsCard("arwen");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(legolas, rumil, arwen, garrison);
		scn.MoveAlliesToTable(orophin, calaglin);
		scn.MoveCardsToSupportArea(bow, bow2);

		var soldier = scn.GetShadowCard("soldier");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, bow, bow2);
		scn.MoveMinionsToTable(soldier, savage);
		scn.AddWoundsToChar(soldier, 1);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		// Rumil is an archer while the wounded Orc Soldier can be spotted.
		assertTrue(scn.HasKeyword(rumil, Keyword.ARCHER));
		assertEquals(7, scn.GetStrength(rumil));
		// Legolas, Rumil and Garrison of Gondor; the allies are away from their home site 6.
		assertEquals(3, scn.GetFreepsArcheryTotal());

		assertTrue(scn.FreepsActionAvailable(bow));
		scn.FreepsUseCardAction(bow);
		// Elf archers, companions and allies; not an Elf who is no archer, nor an archer who is no Elf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, rumil, orophin, calaglin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, garrison));
		scn.FreepsChooseCards(legolas, rumil, orophin);

		// Any minion: a [sauron] Orc and an [isengard] Uruk-hai.
		assertTrue(scn.FreepsHasCardChoiceAvailable(soldier, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(soldier);
		assertInDiscard(soldier, bow);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.SUPPORT, bow2);

		// The three spotted lose archer; Calaglin, not spotted, and Garrison of Gondor keep it.
		assertFalse(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(rumil, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(orophin, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(calaglin, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(garrison, Keyword.ARCHER));
		assertEquals(1, scn.GetFreepsArcheryTotal());
		// One Elf archer is left: the second copy cannot spot three.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bow2));

		// Orophin wounds the Uruk Savage: a wounded minion again, so Rumil is strength +1, but he cannot gain archer.
		assertEquals(6, scn.GetStrength(rumil));
		scn.FreepsUseCardAction(orophin);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(7, scn.GetStrength(rumil));
		assertFalse(scn.HasKeyword(rumil, Keyword.ARCHER));
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// Garrison of Gondor's archery wound goes on the one minion left; once the archery phase is over, the three
		// are archers again.
		scn.PassArcheryActions();
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(rumil, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(orophin, Keyword.ARCHER));
	}

	@Test
	public void IsNotOfferedWithTwoElfArchersBesideAnElfWhoIsNoArcherAndAnArcherWhoIsNoElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var arwen = scn.GetFreepsCard("arwen");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(legolas, arwen, garrison);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToSupportArea(bow);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, bow);
		scn.MoveMinionsToTable(savage);

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bow));
	}
}
