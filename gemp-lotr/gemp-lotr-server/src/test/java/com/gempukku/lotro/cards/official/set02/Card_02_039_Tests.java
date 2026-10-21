package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_039_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beyond", "2_39");

					put("raiding", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3, damage +1 and nothing else
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, vitality 2, damage +1 and nothing else
					put("retainer", "3_57");    // Isengard Retainer: [isengard] Orc, no Uruk-hai; a regroup ability only used on purpose
					put("champion", "11_111");  // Champion Orc: [orc] Orc, toil 2 and nothing else; the bearer of a Shadow armor
					put("gear", "17_90");       // Rider's Gear: [orc] armor possession, bearer an [orc] Orc; bars wounds only on a mounted bearer

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability only used on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: [gondor] Man companion; not overwhelmed unless tripled
					put("merry", "1_302");      // Merry, Friend to Sam: Hobbit companion; a skirmish ability
					put("pippin", "1_306");     // Pippin, Friend to Frodo: Hobbit companion; guards [shire] tales only
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: Hobbit ally, no companion; a fellowship ability

					put("armor", "1_92");       // Armor: [gondor] armor possession, bearer a Man; caps wounds in skirmishes
					put("helm", "1_15");        // Gimli's Helm: [dwarven] helm possession, bearer Gimli
					put("shield", "1_107");     // Great Shield: [gondor] shield possession, bearer a Man; minion archery total -1
					put("orcarmor", "10_113");  // Orc Armor: [shire] armor possession, bearer a Hobbit; acts in skirmishes only
					put("axe", "1_9");          // Dwarven Axe: [dwarven] hand weapon possession, bearer a Dwarf; acts on a skirmish lost to bearer
					put("mithril", "2_105");    // Mithril-coat: [shire] armor ARTIFACT, bearer Frodo; nothing it does matters in the maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeyondtheHeightofMenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Beyond the Height of Men
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert an Uruk-hai to discard an armor possession, helm possession, or shield possession (or all such Free Peoples possessions if you can spot 6 companions).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("beyond");

		assertEquals("Beyond the Height of Men", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithFiveCompanionsItExertsAnUrukhaiToDiscardOneArmorHelmOrShieldPossessionOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beyond = scn.GetShadowCard("beyond");
		var raiding = scn.GetShadowCard("raiding");
		var sentry = scn.GetShadowCard("sentry");
		var retainer = scn.GetShadowCard("retainer");
		var champion = scn.GetShadowCard("champion");
		var gear = scn.GetShadowCard("gear");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var maggot = scn.GetFreepsCard("maggot");
		var armor = scn.GetFreepsCard("armor");
		var helm = scn.GetFreepsCard("helm");
		var shield = scn.GetFreepsCard("shield");
		var orcarmor = scn.GetFreepsCard("orcarmor");
		var axe = scn.GetFreepsCard("axe");
		var mithril = scn.GetFreepsCard("mithril");
		// Five companions with Frodo; Farmer Maggot is an ally, and no companion.
		scn.MoveCompanionsToTable(gimli, aragorn, boromir, scn.GetFreepsCard("merry"));
		scn.MoveAlliesToTable(maggot);
		scn.AttachCardsTo(gimli, helm, axe);
		scn.AttachCardsTo(aragorn, shield);
		scn.AttachCardsTo(boromir, armor);
		scn.AttachCardsTo(maggot, orcarmor);
		scn.AttachCardsTo(frodo, mithril);
		scn.MoveCardsToHand(beyond);
		assertAttachedTo(helm, gimli);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(shield, aragorn);
		assertAttachedTo(armor, boromir);
		assertAttachedTo(orcarmor, maggot);
		assertAttachedTo(mithril, frodo);

		scn.StartGame();
		scn.MoveMinionsToTable(raiding, sentry, retainer, champion);
		scn.AttachCardsTo(champion, gear);
		assertAttachedTo(gear, champion);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(beyond));
		scn.ShadowPlayCard(beyond);

		// An Uruk-hai of [isengard] and one of [sauron]; the [isengard] Orc and the [orc] Orc are not offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiding, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(retainer, champion));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(sentry));
		scn.ShadowChooseCard(sentry);
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(raiding));
		assertEquals(0, scn.GetWoundsOn(retainer));

		// Five companions: one armor, helm or shield possession, of either side, the ally's among them.  Not a
		// hand weapon, and not an armor that is an artifact.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, helm, shield, orcarmor, gear));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axe, mithril));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(helm);

		assertInDiscard(helm, beyond);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(shield, aragorn);
		assertAttachedTo(armor, boromir);
		assertAttachedTo(orcarmor, maggot);
		assertAttachedTo(mithril, frodo);
		assertAttachedTo(gear, champion);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithSixCompanionsEveryFreePeoplesArmorHelmAndShieldPossessionIsDiscardedWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beyond = scn.GetShadowCard("beyond");
		var raiding = scn.GetShadowCard("raiding");
		var champion = scn.GetShadowCard("champion");
		var gear = scn.GetShadowCard("gear");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var maggot = scn.GetFreepsCard("maggot");
		var armor = scn.GetFreepsCard("armor");
		var helm = scn.GetFreepsCard("helm");
		var shield = scn.GetFreepsCard("shield");
		var orcarmor = scn.GetFreepsCard("orcarmor");
		var axe = scn.GetFreepsCard("axe");
		var mithril = scn.GetFreepsCard("mithril");
		// Six companions with Frodo, and the ally.
		scn.MoveCompanionsToTable(gimli, aragorn, boromir, scn.GetFreepsCard("merry"), scn.GetFreepsCard("pippin"));
		scn.MoveAlliesToTable(maggot);
		scn.AttachCardsTo(gimli, helm, axe);
		scn.AttachCardsTo(aragorn, shield);
		scn.AttachCardsTo(boromir, armor);
		scn.AttachCardsTo(maggot, orcarmor);
		scn.AttachCardsTo(frodo, mithril);
		scn.MoveCardsToHand(beyond);
		assertAttachedTo(helm, gimli);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(shield, aragorn);
		assertAttachedTo(armor, boromir);
		assertAttachedTo(orcarmor, maggot);
		assertAttachedTo(mithril, frodo);

		scn.StartGame();
		scn.MoveMinionsToTable(raiding, champion);
		scn.AttachCardsTo(champion, gear);
		assertAttachedTo(gear, champion);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The one Uruk-hai exerts, and the parenthesis, available, is what happens: no possession is chosen.
		assertEquals(0, scn.GetWoundsOn(raiding));
		scn.ShadowPlayCard(beyond);
		assertEquals(1, scn.GetWoundsOn(raiding));

		assertInDiscard(armor, helm, shield, orcarmor, beyond);
		// The Shadow armor, the hand weapon and the artifact stay.
		assertAttachedTo(gear, champion);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(mithril, frodo);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoArmorHelmOrShieldPossessionInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beyond = scn.GetShadowCard("beyond");
		var raiding = scn.GetShadowCard("raiding");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var mithril = scn.GetFreepsCard("mithril");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.AttachCardsTo(frodo, mithril);
		scn.MoveCardsToHand(beyond);

		scn.StartGame();
		scn.MoveMinionsToTable(raiding);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(mithril, frodo);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// An event's target is no requirement: it is played, the Uruk-hai exerts, and nothing is discarded.
		assertTrue(scn.ShadowPlayAvailable(beyond));
		assertEquals(0, scn.GetWoundsOn(raiding));
		scn.ShadowPlayCard(beyond);
		assertEquals(1, scn.GetWoundsOn(raiding));
		assertInDiscard(beyond);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(mithril, frodo);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyUrukhaiIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beyond = scn.GetShadowCard("beyond");
		var raiding = scn.GetShadowCard("raiding");
		var retainer = scn.GetShadowCard("retainer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, shield);
		scn.MoveCardsToHand(beyond);

		scn.StartGame();
		scn.MoveMinionsToTable(raiding, retainer);
		scn.AddWoundsToChar(raiding, 2);
		assertEquals(2, scn.GetWoundsOn(raiding));
		assertAttachedTo(shield, aragorn);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Vitality 3 with 2 wounds; the Isengard Retainer could exert, but is not an Uruk-hai.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(beyond));
	}
}
