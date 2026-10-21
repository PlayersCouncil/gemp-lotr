package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shake", "8_13");
					put("shake2", "8_13");
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is an archery phase
					put("runner2", "1_178");

					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion; archer, so laid only in the fellowship tests
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion; +3 only against a Nazgul
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an Elf ally (cheated in); a fellowship ability used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard; a standing strength bonus only
					put("ent", "6_27");         // Ent Avenger: a [gandalf] Ent companion; a standing strength bonus only
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] companion; a skirmish ability used only on purpose
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] ally; a fellowship ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a [dwarven] ally; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShakeOfftheShadowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Shake Off the Shadow
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Fellowship:</b> Exert an Elf companion and either a [gandalf] companion or a [dwarven] companion to add an [elven] token here.<br><b>Archery:</b> Remove an [elven] token here to make the fellowship archery total +1 (limit +2).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shake");

		assertEquals("Shake Off the Shadow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipAbilityExertsAnElfCompanionAndAGandalfOrDwarvenCompanionToAddATokenToThisCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shake = scn.GetFreepsCard("shake");
		var shake2 = scn.GetFreepsCard("shake2");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var gandalf = scn.GetFreepsCard("gandalf");
		var ent = scn.GetFreepsCard("ent");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, arwen, gandalf, ent, gimli, aragorn);
		scn.MoveAlliesToTable(elrond, barliman, grimir);
		scn.MoveCardsToSupportArea(shake, shake2);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(shake));
		assertEquals(0, scn.GetCultureTokensOn(shake2));
		assertTrue(scn.FreepsActionAvailable(shake));
		scn.FreepsUseCardAction(shake);

		// Elf companions of two titles; not Elrond, an Elf ally, nor a companion of another race.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond, gandalf, ent, gimli, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);

		// Either kind is offered in the one choice: [gandalf] companions of two races and a [dwarven] companion; not
		// the [gandalf] and [dwarven] allies, a [gondor] companion or an Elf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, ent, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, grimir, aragorn, legolas));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gandalf);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetCultureTokensOn(shake));
		assertEquals(0, scn.GetCultureTokensOn(shake2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Again, with the [dwarven] companion.
		scn.FreepsUseCardAction(shake);
		scn.FreepsChooseCard(legolas);
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetCultureTokensOn(shake));
		assertEquals(0, scn.GetCultureTokensOn(shake2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWithEveryElfCompanionExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shake = scn.GetFreepsCard("shake");
		var legolas = scn.GetFreepsCard("legolas");
		var elrond = scn.GetFreepsCard("elrond");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(legolas, gandalf, gimli);
		// Elrond, an Elf ally who could exert, is no companion.
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(shake);
		scn.AddWoundsToChar(legolas, 2);

		scn.StartGame();

		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(shake));
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWithEveryGandalfAndDwarvenCompanionExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shake = scn.GetFreepsCard("shake");
		var arwen = scn.GetFreepsCard("arwen");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, gandalf, gimli, aragorn);
		// A [gandalf] ally, a [dwarven] ally and a [gondor] companion could exert.
		scn.MoveAlliesToTable(barliman, grimir);
		scn.MoveCardsToSupportArea(shake);
		scn.AddWoundsToChar(gandalf, 3);
		scn.AddWoundsToChar(gimli, 2);

		scn.StartGame();

		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(shake));
	}

	@Test
	public void ArcheryAbilityRemovesATokenFromThisCopyToMakeTheFellowshipArcheryTotalPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shake = scn.GetFreepsCard("shake");
		var shake2 = scn.GetFreepsCard("shake2");
		scn.MoveCardsToSupportArea(shake, shake2);
		scn.AddTokensToCard(shake2, 1);

		scn.StartGame();
		assertEquals(0, scn.GetCultureTokensOn(shake));
		assertEquals(1, scn.GetCultureTokensOn(shake2));
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));

		// Not in the maneuver phase.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(shake2));

		// No token on this copy: not offered for it, though the other copy holds one.
		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(shake));
		assertTrue(scn.FreepsActionAvailable(shake2));

		assertEquals(0, scn.GetFreepsArcheryTotal());
		scn.FreepsUseCardAction(shake2);
		assertEquals(0, scn.GetCultureTokensOn(shake2));
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityAtItsLimitStillRemovesATokenAndAddsNothingToTheArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("shake");
		scn.MoveCardsToSupportArea(card);
		scn.AddTokensToCard(card, 3);

		scn.StartGame();
		assertEquals(3, scn.GetCultureTokensOn(card));
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals("false", scn.FreepsActionNoEffect(card));
		scn.FreepsUseCardAction(card);
		scn.ShadowPass();
		scn.FreepsUseCardAction(card);
		scn.ShadowPass();
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetCultureTokensOn(card));

		// Limit +2: a token can still be removed, so the ability is still offered, flagged as doing nothing
		// (Ketura on Q-0041).
		assertTrue(scn.FreepsActionAvailable(card));
		assertEquals("limit", scn.FreepsActionNoEffect(card));

		scn.FreepsUseCardAction(card);
		assertEquals(0, scn.GetCultureTokensOn(card));
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void TheLimitIsEachCopysOwnAndIsRenewedInTheNextArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shake = scn.GetFreepsCard("shake");
		var shake2 = scn.GetFreepsCard("shake2");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(shake, shake2);
		scn.AddTokensToCard(shake, 3);
		scn.AddTokensToCard(shake2, 1);

		scn.StartGame();
		assertEquals(3, scn.GetCultureTokensOn(shake));
		assertEquals(1, scn.GetCultureTokensOn(shake2));
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ARCHERY);

		scn.FreepsUseCardAction(shake);
		scn.ShadowPass();
		scn.FreepsUseCardAction(shake);
		scn.ShadowPass();
		assertEquals(2, scn.GetFreepsArcheryTotal());

		// The first copy is at its +2; the second adds its own +1.
		scn.FreepsUseCardAction(shake2);
		assertEquals(0, scn.GetCultureTokensOn(shake2));
		assertEquals(3, scn.GetFreepsArcheryTotal());

		// The fellowship's archery wound kills the Runner (vitality 1); a second move, to site 3, for a second
		// archery phase this turn.
		scn.PassArcheryActions();
		assertInDiscard(runner);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(scn.GetShadowCard("runner2"));
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetCultureTokensOn(shake));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals("false", scn.FreepsActionNoEffect(shake));
		scn.FreepsUseCardAction(shake);
		assertEquals(0, scn.GetCultureTokensOn(shake));
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
